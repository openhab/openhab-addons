package org.openhab.binding.yamahareceiver.internal.protocol;

import java.io.IOException;
import java.lang.ref.WeakReference;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * Yamaha Receiver protocol related to net radio functionally. No state is saved in this class.
 * Usually you want to create an instance, manipulate or request data and forget.
 *
 * Example:
 *
 * NetRadioMenu menu = new NetRadioMenu(com_object, receiverName);
 * menu.goToPath(menuDir);
 * menu.selectItem(stationName);
 *
 * @author Dennis Frommknecht
 * @author David Graeff
 * @since 2.0.0
 */
public class NetRadioMenu {
    private Document menuState;
    private boolean menuStateRequiresUpdate = true;
    private final WeakReference<YamahaReceiverCommunication> com;
    private final String rootCommand;

    /**
     * Create a NetRadioMenu class for altering and requesting the current net radio channel.
     * 
     * @param com The Yamaha communication object to send http requests.
     * @param modelTypeName Some models support a different set of features or need different commands.
     */
    public NetRadioMenu(YamahaReceiverCommunication com, String modelTypeName) {
        this.com = new WeakReference<YamahaReceiverCommunication>(com);

        if (modelTypeName.matches("^(?:RX-A\\d{1,2}10|RX-A\\d{1,2}00|RX-V\\d{1,2}(?:71|67|65))$")) {
            rootCommand = "Back to Home";
        } else {
            rootCommand = "Return to Home";
        }
    }

    public boolean isInDirectory(String path) throws IOException {
        String[] pathArr = path.split("/");
        // Full path info not available, so guess from last path element and number of path elements
        return getMenuName().equals(pathArr[pathArr.length - 1]) && getLevel() == pathArr.length;
    }

    public void goToRoot() throws IOException {
        if (getLevel() > 0) {
            com.get().postAndGetResponse("<YAMAHA_AV cmd=\"PUT\"><NET_RADIO><List_Control><Cursor>" + rootCommand
                    + "</Cursor></List_Control></NET_RADIO></YAMAHA_AV>");
            menuChanged();
        }
    }

    public void goToPage(int page) throws IOException {
        int line = (page - 1) * 8 + 1;
        com.get().postAndGetResponse("<YAMAHA_AV cmd=\"PUT\"><NET_RADIO><List_Control><Jump_Line>" + line
                + "</Jump_Line></List_Control></NET_RADIO></YAMAHA_AV>");
        menuChanged();
    }

    public void goToPath(String fullPath) throws IOException {
        if (!isInDirectory(fullPath)) {
            goToRoot();

            String[] pathArr = fullPath.split("/");
            for (String pathElement : pathArr) {
                selectItem(pathElement);
            }
        }
    }

    public String getMenuName() throws IOException {
        Node menuNameNode = getStateNode("List_Info/Menu_Name");
        return menuNameNode != null ? menuNameNode.getTextContent() : null;
    }

    public int getLevel() throws IOException {
        Node menuLayerNode = getStateNode("List_Info/Menu_Layer");
        return menuLayerNode != null ? Integer.parseInt(menuLayerNode.getTextContent()) - 1 : -1;
    }

    public int getPageNumber() throws IOException {
        Node node = getStateNode("List_Info/Cursor_Position/Current_Line");
        if (node != null) {
            int currentLine = Integer.parseInt(node.getTextContent());
            return (currentLine - 1) / 8;
        }
        return 0;
    }

    public int getNumberOfPages() throws IOException {
        Node node = getStateNode("List_Info/Cursor_Position/Max_Line");
        if (node != null) {
            int maxLines = Integer.parseInt(node.getTextContent());
            return (int) Math.ceil(maxLines / 8d);
        }
        return 0;
    }

    public void selectItem(String name) throws IOException {
        for (int page = 1; page <= getNumberOfPages(); page++) {
            if (getPageNumber() != page) {
                goToPage(page);
            }

            int index = findItemOnCurrentPage(name);
            if (index > 0) {
                com.get().postAndGetResponse("<YAMAHA_AV cmd=\"PUT\"><NET_RADIO><List_Control><Direct_Sel>Line_" + index
                        + "</Direct_Sel></List_Control></NET_RADIO></YAMAHA_AV>");
                menuChanged();
                return;
            }
        }

        throw new IOException("Item '" + name + "' doesn't exist in menu " + getMenuName());
    }

    private int findItemOnCurrentPage(String itemName) throws IOException {
        for (int i = 1; i <= 8; i++) {
            Node node = getStateNode("List_Info/Current_List/Line_" + i + "/Txt");
            if (node != null && node.getTextContent().equals(itemName)) {
                return i;
            }
        }

        return -1;
    }

    private Node getStateNode(String path) throws IOException {
        return YamahaReceiverCommunication.getNode(getMenuState().getFirstChild(), path);
    }

    private Document getMenuState() throws IOException {
        if (menuStateRequiresUpdate) {
            refreshMenuState();
        }
        return this.menuState;
    }

    private void menuChanged() {
        this.menuStateRequiresUpdate = true;
    }

    private void refreshMenuState() throws IOException {
        int totalWaitingTime = 0;

        Document doc;

        while (true) {
            doc = com.get().postAndGetXmlResponse(
                    "<YAMAHA_AV cmd=\"GET\"><NET_RADIO><List_Info>GetParam</List_Info></NET_RADIO></YAMAHA_AV>");
            Node node = YamahaReceiverCommunication.getNode(doc.getFirstChild(), "List_Info/Menu_Status");

            if (node == null || node.getTextContent().equals("Ready")) {
                break;
            }

            totalWaitingTime += YamahaReceiverCommunication.MENU_RETRY_DELAY;
            if (totalWaitingTime > YamahaReceiverCommunication.MENU_MAX_WAITING_TIME) {
                throw new IOException(
                        "Menu still not ready after " + YamahaReceiverCommunication.MENU_MAX_WAITING_TIME + "ms");
            }

            try {
                Thread.sleep(YamahaReceiverCommunication.MENU_RETRY_DELAY);
            } catch (InterruptedException e) {
                // Ignore and just retry immediately
            }
        }

        this.menuState = doc;
        this.menuStateRequiresUpdate = false;
    }
}