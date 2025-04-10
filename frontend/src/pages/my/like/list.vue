<template>
    <div>
        <PostListComponent 
        :path="path"
        :postListPath="postListPath"/>
    </div>
</template>

<script>
import PostListComponent from '@/components/post/PostListComponent.vue';

export default {
    name: "MyLikeList",
    data() {
        return {
            path : '/my/like/list',
            postListPath : `${this.$store.state.backendUrl}/api/my/search`, 
        };
    },
    methods: {},
    components: {
        PostListComponent,
    },

};
</script>