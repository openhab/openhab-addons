import axios from "axios";
import { OHAuthHelper } from "./auth";

class ActionTemplateInterpreterHttpAPI {
    private ohAuthHelper = new OHAuthHelper({ path: '/actiontemplatehli', ohUrl: async () => this.getUrlOpenHAB() });
    private authToken: string | undefined;
    async authorize() {
        // check security and redirect to login
        try {
            const authorized = await this.ohAuthHelper.tryExchangeAuthorizationCode();
            await this.ohAuthHelper.refreshAccessToken((err, data) => {
                if (err) {
                    return this.unauthorized();
                } else if (data) {
                    this.authToken = data.access_token;
                }
            }, !authorized);
        } catch (error) {
            console.error(error);
            return this.unauthorized();
        }
    }
    async getActions() {
        return (await axios.get<ActionTemplateConfig[]>(`${this.getUrlOpenHAB()}/rest/actiontemplatehli/actions`, { headers: this.getDefaultHeaders() })).data;
    }
    async createAction(action: ActionTemplateConfig): Promise<ActionTemplateConfig> {
        return (await axios.post<ActionTemplateConfig>(`${this.getUrlOpenHAB()}/rest/actiontemplatehli/actions`, action, { headers: this.getDefaultHeaders() })).data;
    }
    async updateAction(id: string, action: ActionTemplateConfig) {
        return (await axios.put<ActionTemplateConfig>(`${this.getUrlOpenHAB()}/rest/actiontemplatehli/actions/${id}`, action, { headers: this.getDefaultHeaders() })).data;
    }
    async deleteAction(id: string) {
        (await axios.delete(`${this.getUrlOpenHAB()}/rest/actiontemplatehli/actions/${id}`, { headers: this.getDefaultHeaders() }));
    }
    async getPlaceholders() {
        return (await axios.get<ActionTemplatePlaceholder[]>(`${this.getUrlOpenHAB()}/rest/actiontemplatehli/placeholders`, { headers: this.getDefaultHeaders() })).data;
    }
    async createPlaceholder(action: ActionTemplatePlaceholder): Promise<ActionTemplatePlaceholder> {
        return (await axios.post<ActionTemplatePlaceholder>(`${this.getUrlOpenHAB()}/rest/actiontemplatehli/placeholders`, action, { headers: this.getDefaultHeaders() })).data;
    }
    async updatePlaceholder(label: string, action: ActionTemplatePlaceholder) {
        return (await axios.put<ActionTemplatePlaceholder>(`${this.getUrlOpenHAB()}/rest/actiontemplatehli/placeholders/${label}`, action, { headers: this.getDefaultHeaders() })).data;
    }
    async deletePlaceholder(label: string) {
        (await axios.delete(`${this.getUrlOpenHAB()}/rest/actiontemplatehli/placeholders/${label}`, { headers: this.getDefaultHeaders() }));
    }
    async test(text: string, dryRun: boolean) {
        return (await axios.post<{ response: string, actionTemplate: string, itemName: string, }>(`${this.getUrlOpenHAB()}/rest/actiontemplatehli/test`, { text, dryRun }, { headers: this.getDefaultHeaders() })).data;
    }
    private getUrlOpenHAB(): string {
        let port = '';
        if (!((location.protocol === 'https:' && location.port === '443') || (location.protocol === 'http:' && location.port === '80'))) {
            port = `:${location.port}`
        }
        return `${location.protocol}//${location.hostname}${port}`
    }
    private getDefaultHeaders() {
        const headers = {
            accept: "application/json",
        } as { [key: string]: string };
        if (this.authToken?.length) { headers["Authorization"] = "Bearer " + this.authToken; }
        return headers;
    }
    private unauthorized() {
        console.debug("Unauthorized, redirecting to login");
        if (!(import.meta as any).env.DEV) {
            this.ohAuthHelper.authorize();
        } else {
            console.warn("Login redirection disabled in dev mode");
        }
    }
}

export const httpAPI = new ActionTemplateInterpreterHttpAPI();

export interface ActionTemplateConfig {
    id: string,
    template: string
    placeholders: ActionTemplatePlaceholder[],
    read: boolean,
    value: string,
    silent: boolean,
    emptyValue: string,
    requiredTags: string[],
    affectedTypes: string[],
    affectedSemantics: string[],
    groupTargets?: {
        affectedTypes: string[],
        affectedSemantics: string[],
        requiredTags: string[],
        mergeState: boolean,
        recursive: boolean,
    }
}

export interface ActionTemplatePlaceholder {
    label: string,
    values: string[],
    mappedValues: { [tokensString: string]: string }
}