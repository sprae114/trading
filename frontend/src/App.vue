<template>
  <v-app>
    <NavigationDrawer />
    <v-main>
      <v-alert v-if="isAlertOpen" :type="alertTpye"> {{ alertMessage }} </v-alert>
      <router-view />
    </v-main>
  </v-app>
</template>

<script>
import { mapState, mapMutations } from 'vuex';
import NavigationDrawer from '@/components/NavigationDrawer.vue';

export default {
  name: 'App',
  components: {
    NavigationDrawer
  },
  methods: {
    ...mapMutations(['SHOW_NOMAL_ALERT_MESSAGE']),
  },
  computed: {
    ...mapState(['isAlertOpen', 'alertTpye', 'alertMessage']),
  },
};
</script>

<style>
.v-alert {
  top: 0;
  left: 0;
  right: 0;
  z-index: 1000;
}
</style>