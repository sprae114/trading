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
            path : '/my/like/list',
        };
    },
    methods: {},
    components: {
        PostDetailComponent,
    },

};
</script>