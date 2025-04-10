<template>
    <ChatRoomModalComponent :path="path"/>
</template>

<script>
import ChatRoomModalComponent from '@/components/chat/ChatRoomModalComponent.vue';

export default {
    name: 'ChatRoomComponent',
    data() {
        return {
            path : '/post/list',
        };
    },
    components: {
        ChatRoomModalComponent
    },
};
</script>
