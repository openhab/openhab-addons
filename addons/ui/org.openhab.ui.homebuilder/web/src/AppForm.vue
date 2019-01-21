<template>
    <div class='row'>
        <div class='col-sm-6' id="form">
            <vue-form-generator :schema='schema' :model='model' :options='formOptions'>
            </vue-form-generator>
            <pre class='language-json d-none'><code>{{ model }}</code></pre>
        </div>
        <div class='col-sm-6'>
            <affix id="preview" relative-element-selector="#form" :offset="{ top: 65, bottom: 0 }">
                <collapse v-if="model && model.filesGenerated.includes('items')" :uid="'items'" :heading="'Items'"
                          :content='model.itemsType === "text" ? generateItems(model) : generateItemsJson(model)'></collapse>

                <collapse v-if="model && model.filesGenerated.includes('sitemap')" :uid="'sitemap'" :heading="'Sitemap'"
                          :content='generateSitemap(model)'></collapse>

                <collapse v-if="model && model.filesGenerated.includes('habpanel')" :uid="'habpanel'" :heading="'HABPanel Dashboard'"
                          :content='generateDashboard(model)'></collapse>
            </affix>
        </div>
    </div>
</template>

<style lang="scss" src="./scss/app.scss">
</style>

<script>
    import * as _ from 'lodash'
    import Collapse from './Collapse.vue'
    import {component as VueFormGenerator} from 'vue-form-generator'
    import {floors, languages, objects, OBJECTS_SUFFIX, rooms} from './definitions'
    import * as schema from './formSchema'
    import {roomsSelect} from './formSchema'
    import {objectSelect} from './formSchema'
    import {generateItems} from './textItems'
    import {generateItemsJson} from './restItems'
    import {generateDashboard} from './habpanel'
    import {generateSitemap} from './sitemap'

    export default {
        components: {
            vueFormGenerator: VueFormGenerator,
            collapse: Collapse
        },

        data() {
            return {
                model: {
                    language: 'en-UK',
                    homeName: 'Our Home',
                    filesGenerated: ['items', 'sitemap'],
                    itemsType: 'text',
                    itemsChannel: true,
                    itemsIcons: true,
                    itemsTags: true,
                    floors: []
                },

                formOptions: {
                    validateAfterLoad: true,
                    validateAfterChanged: true,
                    fieldIdPrefix: 'user-'
                }
            }
        },

        computed: {
            schema() {
                let roomsFields = [];
                this.model.floors.forEach((floor) => {
                    roomsFields.push(roomsSelect(floor.value, floor.name || floor.value));
                });

                let objectsFields = [];
                this.model.floors.forEach((floor) => {
                    if (this.model[floor.value]) {
                        this.model[floor.value].forEach((element) => {
                            let name = (element.name || element.value)  + ' (' + floor.name + ')';
                            let fieldName = floor.value + '_' + element.value + OBJECTS_SUFFIX;
                            objectsFields.push(objectSelect(fieldName, name));
                        });
                    }
                });

                return {
                    groups: [
                        {legend: '', fields: schema.basicFields},
                        {legend: 'Floors', fields: schema.floorsFields},
                        {legend: 'Rooms', fields: roomsFields},
                        {legend: 'Objects', fields: objectsFields},
                        {legend: '', fields: schema.settingsFields}
                    ]
                };
            }
        },

        methods: {
            /**
             * Generates textual Items
             */
            generateItems,

            /**
             * Generates JSON array of Items,
             * understandable by REST API
             */
            generateItemsJson,

            /**
             * Generates textual Sitemap
             */
            generateSitemap,

            /**
             * Generates HABPanel dashboard JSON config
             */
            generateDashboard,

            /**
             * Gets i18n configuration from ESH service
             */
            getLocale: function () {
                const DEFAULT_LOCALE = 'en-UK';
                this.$http
                    .get('services/org.eclipse.smarthome.core.i18nprovider/config')
                    .then(response => {
                        const body = response.body;
                        let selectedLang = DEFAULT_LOCALE;

                        if (body.language && body.region) {
                            let langId = body.language + '-' + body.region;
                            let lang = _.find(languages, {id: langId});
                            selectedLang = lang ? langId : DEFAULT_LOCALE;
                        }

                        this.$data.model.language = selectedLang;
                        this.fetchTranslations(selectedLang);
                    })
                    .catch(reason => {
                        this.$data.model.language = DEFAULT_LOCALE;
                        this.fetchTranslations(DEFAULT_LOCALE);
                    });
            },

            /**
             * Loads translation file from the `/i18n/` folder
             * and assigns new `name` properties to the definitions.
             */
            fetchTranslations: function (language) {
                let root = window.location.href.replace(/\/index.html.*/, '');
                this.$http
                    .get(root + '/i18n/' + language + '.json')
                    .then(response => {
                        this.$i18n.locale = language;
                        this.$i18n.setLocaleMessage(language, response.body);

                        let stack = [...rooms, ...objects];
                        let roomsModel = _.chain(this.$data.model)
                            .pickBy((value, key) => _.endsWith(key, OBJECTS_SUFFIX))
                            .value();

                        floors.forEach(function (item) {
                            stack = [...stack, item];

                            if (this.$data.model[item.value]) {
                                stack = [...stack, ...this.$data.model[item.value]];
                            }
                        }.bind(this));

                        if (!_.isEmpty(roomsModel)) {
                            _.forOwn(roomsModel, (room) => {
                                stack = [...stack, ...room]
                            });
                        }

                        stack.forEach(item => {
                            if (!item.custom) {
                                item.name = this.$i18n.t(item.value);
                            }
                        });
                        this.$forceUpdate();
                    });
            },

            resizeAffix(event) {
                let bodyWidth = document.body && document.body.clientWidth;
                let formEl = document.getElementById('form');
                let previewEl = document.getElementById('preview');
                let formWidth = formEl && form.clientWidth;
                let hidden = 'd-none';

                previewEl.style.width = formWidth + 'px';

                if (bodyWidth <= 599) {
                    previewEl.classList.add(hidden);
                    document.querySelector('.navbar-toggler-icon').classList.remove(hidden);
                    document.querySelector('.navbar-close-icon').classList.add(hidden);
                } else {
                    previewEl.classList.remove(hidden);
                }
            },
        },

        http: {
            root: window.location.origin + '/rest/'
        },

        mounted: function () {
            this.getLocale();

            this.$nextTick(function () {
                window.addEventListener('resize', this.resizeAffix);
                this.resizeAffix();
            })
        },

        watch: {
            locale(val) {
                this.$i18n.locale = val
            }
        },

        beforeDestroy() {
            window.removeEventListener('resize', this.resizeAffix);
        }
    }
</script>
