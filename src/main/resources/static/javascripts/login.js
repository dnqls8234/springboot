// 쿠키 값 가져오기
var id_cookie = cookie.get('id_cookie');
if (id_cookie) {
    $('#login_id').val(id_cookie);
}

var hpOpen = function (url) {
    window.open("https://www.facetwo.co.kr" + url, 'openWin');
};

var login_call = function (e) {
    if (e.keyCode == 13) login();
};

var login = function () {
    var id = $('#login_id').val();
    var pwd = $('#login_pwd').val();

    if (id == '') {
        alert('로그인 아이디를 입력하세요');
        $('#login_id').focus();
        return false;
    }

    if (pwd == '') {
        alert('로그인 비밀번호를 입력하세요');
        $('#login_pwd').focus();
        return false;
    }

    if (/[\s]/g.test(id)) {
        alert('비밀번호에 공백이 있습니다.');
        $('#login_pwd').focus();
        return false;
    }

    if ($('#idSave').is(':checked')) {
        cookie.set('id_cookie', id, 365);
    } else {
        cookie.set('id_cookie', '', -1);
    }

    $.ajax(
        {
            type: 'POST',
            url: '/login/login',
            data: {
                user_id: id,
                password: pwd
            },
            success: function (data) {
                if (data.login_success) {
                    window.location.href = getBaseUrl() + '/main';
                } else {
                    if(data.error_msg) {
                        alert(data.error_msg);
                    } else {
                        alert("로그인 아이디 또는 비밀번호가 맞지 않습니다.");
                    }
                }
            }
        }
    );
};