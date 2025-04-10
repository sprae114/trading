<template>
    <ChatRoomModalComponent :path="path"/>
</template>

<script>
import ChatRoomModalComponent from '@/components/chat/ChatRoomModalComponent.vue';

export default {
    data() {
        return {
            path : '/my/like/list',
        };
    },
    components: {
        ChatRoomModalComponent
    },
};
</script>
