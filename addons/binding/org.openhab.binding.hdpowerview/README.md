# Hunter Douglas PowerView Binding

This is an openHAB binding for the [Hunter Douglas PowerView Motorized Shades](http://www.hunterdouglas.com/operating-systems/powerview-motorization/support) via the PowerView Hub.

PowerView shades have motorization control for their vertical position, as well as vane controls on the shade's slats.
Make sure your Shades are visible in the dedicated PowerView app before attempting discovery.
This binding also supports Scenes that are defined via the PowerView app.
This helps to work around a limitation of the Hub - commands are executed serially with a several second delay between executions.
By using a Scene to control multiple shades at once, the shades will all begin moving at the same time.

## Supported Things

<table>
 <tr>
  <td><b>Thing</b></td>
  <td><b>Thing Type</b></td>
  <td><b>Description</b></td>
 </tr>
 <tr>
  <td>PowerView Hub</td>
  <td>Bridge</td>
  <td>The PowerView Hub provides the interface between your network and the shade's radio network.
  It also contains channels used to interact with scenes.</td>
 </tr>
 <tr>
  <td>PowerView Shade</td>
  <td>Thing</td>
  <td>A single motorized shade</td>
 </tr>
</table>

## Discovery

The PowerView Hub is discovered via a NetBios query.
This is the same method used by the dedicated PowerView app.
After the Hub is added, Shades and Scenes will be discovered by querying the Hub.

## Thing Configuration

PowerView things should be configured via discovery - it would be difficult to configure manually as the IDs of the shades and scenes are not exposed via the dedicated app.
However, the configuration parameters are described below:

<table>
 <tr>
  <td><b>Thing</b></td>
  <td><b>Configuration Parameters</b></td>
 </tr>
 <tr>
  <td>PowerView Hub</td>
  <td>
   <table>
    <tr><td><b>host</b> - the hostname or IP address of the Hub on your network.</td></tr>
    <tr><td><b>refresh</b> - the number of milliseconds between fetches of the PowerView Hub's shade state. Defaults to 60,000 (one minute).</td></tr>
   </table>
  </td>
 </tr>
 <tr>
  <td>PowerView Shade</td>
  <td>
   <table>
    <tr><td><b>id</b> - the ID of the PowerView Shade on the Hub.</td></tr>
   </table>
  </td>
 </tr>
</table>

## Channels

### PowerView Shade

<table>
 <tr>
  <td><b>Channel</b></td>
  <td><b>Item Type</b></td>
  <td><b>Description</b></td>
 </tr>
 <tr>
  <td>position</td>
  <td>Rollershutter</td>
  <td>The vertical position of the shade. Up/Down commands will move the shade to its completely up or completely down position. Move/Stop commands are ignored.</td>
 </tr>
 <tr>
  <td>vane</td>
  <td>Dimmer</td>
  <td>The amount the slats on the shade are open. Setting this value will completely close the shade first, as the slats can only be controlled in that position.</td>
 </tr>
 <tr>
  <td>batteryLow</td>
  <td>Switch</td>
  <td>Indicates ON when the battery level of the shade is low, as determined by the Hub's internal rules</td>
 </tr>
</table>

### PowerView Scene

Scenes channels are added to the Hub as they are discovered.

<table>
 <tr>
  <td><b>Channel</b></td>
  <td><b>Item Type</b></td>
  <td><b>Description</b></td>
 </tr>
 <tr>
  <td>activate</td>
  <td>Switch</td>
  <td>Turning this to ON will activate the scene. Scenes are stateless in the PowerView hub - they have no on/off state. Include { autoupdate="false" } on your item configuration to avoid needing to toggle off and on.</td>
 </tr>
</table>
