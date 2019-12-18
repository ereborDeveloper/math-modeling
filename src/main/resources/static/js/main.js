const axios = require('axios');
var app = new Vue({
    el: '#app',
    data: {
        message: 'Привет, Vue!',
        info: ''
    },
    mounted() {
        axios
            .get('http://localhost:8080/modeling/status')
            .then(response => (this.info = response));
    }
});