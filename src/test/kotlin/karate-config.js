function fn() {

    var config = {};
    var env = karate.env;
    if (!env) {
        env = 'dev';
    }

    if (env === 'dev') {

    }
    return config;
}