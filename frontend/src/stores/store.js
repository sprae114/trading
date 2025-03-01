import { createStore } from 'vuex';

const store = createStore({
    state(){
        return {
            backendUrl: 'http://localhost:8080',

            // 로그인 관련
            isLoggedIn : false,
            isOTPVaild: false,
            
            // 모달 관련
            isModalOpen: false,
            modalType: '',
            modalMessage: '',

            // 알람관련
            isAlertOpen: false,
            isBtnAlertVisible: false,
            alertType: '',
            alertMessage: '',
        }
    },
    mutations :{
        // 모달 관련
        SET_MODAL_OPEN(state, payload) {
            state.isModalOpen = payload
        },
        
        // 알람 관련
        SHOW_NOMAL_ALERT(state) {
            state.isAlertOpen = true;
        
            setTimeout(() => {
                state.isAlertOpen = false;
            }, 2000);
        },
        
        SHOW_NOMAL_ALERT_MESSAGE(state, [payload1, payload2]) {
            state.alertTpye = payload1;
            state.alertMessage = payload2;
        
            state.isAlertOpen = true;
        
            setTimeout(() => {
                state.isAlertOpen = false;
            }, 2000);
        },
        
        
        SET_NOMAL_ALERT(state, payload) {
            state.isAlertOpen = payload;
        },
        
        SET_BTN_ALERT(state, payload) {
            state.isBtnAlertVisible = payload;
        },
        
        // OTP 관련
        SET_OTP_VAILD(state, payload) {
            state.isOTPVaild = payload;
        },
        
        SET_IS_LOIGIN(state, payload) {
            state.isLoggedIn = payload;
        },
    },
        actions : {
            // ajax 요청하는 곳
            
    }
})

export default store