import { createStore } from 'vuex';

const store = createStore({
    state(){
        return {
            // backendUrl: 'http://localhost:8080',
            // backendUrl: 'http://localhost:8081',
            backendUrl: 'http://localhost:3000',

            //네이게이션바 관련
            isDrawerOpen: false,

            // 로그인 관련
            isLoggedIn : false,
            isOTPValid: false,
            customerInfo: null,
            
            // 모달 관련
            isModalOpen: false,
            modalType: '',
            modalMessage: '',

            // 알람관련
            isAlertOpen: false,
            isBtnAlertVisible: false,
            alertType: '',
            alertMessage: '',

            // 상품
            showProduct : {},
        }
    },
    mutations :{
        // 네비게이션바 관련
        TOGGLE_DRAWER(state) {
            state.isDrawerOpen = !state.isDrawerOpen;
        },
        
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
            state.alertType = payload1;
            state.alertMessage = payload2;
        
           // 기존 타이머가 있으면 지우기
            if (state.alertTimeout) {
                clearTimeout(state.alertTimeout);
            }

            // 알람 열기
            state.isAlertOpen = true;

            // 새 타이머 설정
            state.alertTimeout = setTimeout(() => {
                state.isAlertOpen = false;
                state.alertTimeout = null; // 타이머 종료 후 초기화
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
            state.isOTPValid = payload;
        },
        
        SET_IS_LOIGIN(state, payload) {
            state.isLoggedIn = payload;
        },

        GET_CUSTOMER_INFO(state, payload) {
            const storedData = sessionStorage.getItem('userInformation');
            
            if (storedData) {
                const parsedData = JSON.parse(storedData);
                console.log(parsedData.customer);
                state.customerInfo = parsedData.customer;
            }
            else {
                state.customerInfo = null;
                this.$store.commit('SET_MODAL_OPEN', true);
                this.$store.commit('SET_MODAL_MESSAGE', '로그인이 필요한 서비스입니다.');
            }
        },

        // 상품
        SET_SHOWPRODUCT(state, payload) {
            state.showProduct = payload;
        }
    },
    getters: {
        getShowProduct(state) {
            return state.showProduct;
        },
    },
})

export default store