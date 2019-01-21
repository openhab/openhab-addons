import Vue from 'vue'
import BootstrapVue from 'bootstrap-vue/dist/bootstrap-vue.esm'
import VueFormGenerator from 'vue-form-generator'
import Affix from 'vue-affix'
import Multiselect from 'vue-multiselect'
import VueResource from 'vue-resource'
import VueI18n from 'vue-i18n'
import 'daemonite-material/css/material.css'
import 'bootstrap-vue/dist/bootstrap-vue.css'
import 'vue-form-generator/dist/vfg.css'
import 'vue-multiselect/dist/vue-multiselect.min.css'
import App from './App.vue'
import fieldMultiselect from './fieldMultiselect.vue'

Vue.use(BootstrapVue);
Vue.use(VueFormGenerator);
Vue.use(Affix);
Vue.use(VueResource);
Vue.use(VueI18n);
Vue.component('multiselect', Multiselect);
Vue.component('fieldMultiselect', fieldMultiselect);

let i18n = new VueI18n({
    locale: 'en-UK'
});

const vm = new Vue({
    i18n,
    el: '#app',
    render: h => h(App)
});
