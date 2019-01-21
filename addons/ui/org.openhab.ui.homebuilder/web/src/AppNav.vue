<template>
    <nav class="navbar fixed-top">
        <a class="navbar-brand mr-auto" href="#">HomeBuilder</a>

        <button @click="togglePreview" type="button" aria-label="Toggle preview" class="navbar-toggler navbar-toggler-right">
            <span class="navbar-toggler-icon"></span>
            <span class="navbar-close-icon d-none">&times;</span>
        </button>

    </nav>
</template>

<style lang="scss">
    .navbar {
        background: #E64A19;
        color: #fff;
    }

    .navbar-toggler {
        display: none;
    }

    .navbar-close-icon {
        color: #200f04;
        font-size: 36px;
        padding-left: 1px;
        margin-top: -8px;
    }
</style>

<script>
    export default {
        methods: {
            togglePreview() {
                document.getElementById('preview').classList.toggle('d-none');
                document.querySelector('.navbar-close-icon').classList.toggle('d-none');
                document.querySelector('.navbar-toggler-icon').classList.toggle('d-none');
            }
        }
    }
</script>
