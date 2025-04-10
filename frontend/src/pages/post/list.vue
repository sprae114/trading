<template>
    <div>
        <PostListComponent 
        :path="path"
        :postListPath="postListPath"
        />
    </div>
</template>

<script>
import PostListComponent from '@/components/post/PostListComponent.vue';

export default {
    name: "PostList",
    data() {
        return {
            path : '/post/list',
            postListPath : `${this.$store.state.backendUrl}/api/post/search`,
        };
    },
    methods: {},
    components: {
        PostListComponent,
    },

};
</script>