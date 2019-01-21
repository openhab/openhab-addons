<template>
    <div>
        <app-nav></app-nav>
        <div class="container" id="app">
            <app-form></app-form>
        </div>
    </div>
</template>

<script>
    import AppNav from './AppNav.vue'
    import AppForm from './AppForm.vue'

    export default {
        name: 'app',
        components: {
            appNav: AppNav,
            appForm: AppForm
        }
    }
</script>