<template>
    <div>
        <PostDetailComponent :path="path"/>
    </div>
</template>

<script>
import PostDetailComponent from '@/components/post/PostDetailComponent.vue';

export default {
    data() {
        return {
            path : '/post/list',
        };
    },
    methods: {},
    components: {
        PostDetailComponent,
    },

};
</script>