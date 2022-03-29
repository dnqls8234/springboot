// 기존 프론트단 ajax url부분을 절대 경로로 처리하여 도메인 처리 부분을 추가함.

// 실행될 환경에서 서브도메인을 사용할 경우 sub_domain 변수에 값을 설정한다.
// 서브도메인을 사용하지 않을 경우 공백으로 지정하면 된다.
function getBaseUrl() {
	var pathArray = location.href.split('/');
	var protocol = pathArray[0];
	var domain = pathArray[2];
	
	// local 환경에서 외장 톰캣으로 실행할 경우, production 환경으로 배포할 경우
//	var sub_domain = 'lwb';
	
	// local 환경에서 내장 톰캣으로 실행할 경우
	var sub_domain = null;
	
	var url = protocol + '//' + domain;
	
	if (sub_domain) {
		url += '/' + sub_domain;
	}

	return url;
}

$.ajaxSetup({
    cache: false,
    beforeSend: function(xhr, options) { 
        options.url = getBaseUrl() + options.url;
    },
    statusCode: {
        302: function (response) {
            var redirect_url = response.getResponseHeader('X-Ajax-Redirect-Url');
            if (redirect_url) {
            	window.location.href = getBaseUrl() + redirect_url;
            }
        }
    },
    error: function (error) {
    	alert(error.responseJSON.message);
    }
    
});


// 카카오스토어 엑셀 모든 데이터가 HTML Entity 형태로 되어 있음
var decodeHtmlEntity = function(str) {
    return str.replace(/&#(\d+);/g, function(match, dec) {
        return String.fromCharCode(dec);
    });
};

var cookie = {
    // 쿠키 생성
    set: function (name, value, day) {
        var expire = new Date();
        expire.setDate(expire.getDate() + day);
        cookies = name + '=' + value + '; path=/ '; // 한글 깨짐을 막기위해 escape(cValue)를 합니다.
        if(typeof day != 'undefined') cookies += ';expires=' + expire.toGMTString() + ';';
        document.cookie = cookies;
    },
    // 쿠키 가져오기
    get: function (name) {
        name = name + '=';
        var cookieData = document.cookie;
        var start = cookieData.indexOf(name);
        var value = '';
        if(start != -1){
            start += name.length;
            var end = cookieData.indexOf(';', start);
            if(end == -1)end = cookieData.length;
            value = cookieData.substring(start, end);
        }
        return value;
    }
};


var get_mall_user_shortcuts = function () {
    var deferred = $.Deferred();

    $.ajax(
        {
            type: 'POST',
            url: '/common/get_mall_user_shortcuts',
            success: function (data) {
                deferred.resolve(data);
            }
        }
    );

    return deferred.promise();
};

var get_customer_admin_shortcuts = function () {
    var deferred = $.Deferred();

    $.ajax(
        {
            type: 'POST',
            url: '/common/get_customer_admin_shortcuts',
            success: function (data) {
                deferred.resolve(data);
            }
        }
    );

    return deferred.promise();
};

var get_malls = function () {
    var deferred = $.Deferred();

    $.ajax({
    	type: 'POST',
        url: '/common/get_malls',
        success: function (data) {
            deferred.resolve(data);
        }
    });

    return deferred.promise();
};

var get_all_malls = function () {
    var deferred = $.Deferred();

    $.ajax({
    	type: 'POST',
        url: '/common/get_all_malls',
        success: function (data) {
            deferred.resolve(data);
        }
    });

    return deferred.promise();
};

var get_mall_users = function (mall_id, mall_user_id) {
    var deferred = $.Deferred();

    $.ajax(
        {
            type: 'POST',
            url: '/common/get_mall_users',
            data: {
                mall_id: mall_id,
                id: mall_user_id
            },
            success: function (data) {
                deferred.resolve(data);
            }
        }
    );

    return deferred.promise();
};

var get_codes = function (title) {
    var deferred = $.Deferred();

    $.ajax({
    	type: 'POST',
        url: '/common/get_codes',
        data: {
            code_title: title
        },
        success: function (data) {
            deferred.resolve(data);
        }
    });

    return deferred.promise();
};

var get_suppliers = function () {
    var deferred = $.Deferred();

    $.ajax({
    	type: 'POST',
        url: '/common/get_suppliers',
        success: function (data) {
            deferred.resolve(data);
        }
    });

    return deferred.promise();
};

var get_logistics = function () {
    var deferred = $.Deferred();

    $.ajax({
    	type: 'POST',
        url: '/common/get_logistics',
        success: function (data) {
            deferred.resolve(data);
        }
    });

    return deferred.promise();
};

var get_domesin_categories = function () {
    var deferred = $.Deferred();

    $.ajax({
    	type: 'POST',
        url: '/common/get_domesin_categories',
        success: function (data) {
            deferred.resolve(data);
        }
    });

    return deferred.promise();
};

var get_excel_fields = function () {
    var deferred = $.Deferred();

    $.ajax({
    	type: 'POST',
        url: '/common/get_excel_fields',
        success: function (data) {
            deferred.resolve(data);
        }
    });

    return deferred.promise();
};

var get_order_upload_excel_form = function (mall_id) {
    var deferred = $.Deferred();

    $.ajax({
    	type: 'POST',
        url: '/common/get_order_upload_excel_form',
        data: {
            mall_id: mall_id
        },
        success: function (data) {
            deferred.resolve(data);
        }
    });

    return deferred.promise();
};

var get_image_orders = function () {
    var deferred = $.Deferred();

    $.ajax(
        {
            type: 'POST',
            url: '/common/get_image_orders',
            success: function (data) {
                deferred.resolve(data);
            }
        }
    );

    return deferred.promise();
};

var getByteLength = function (s) {
	var b = null;
	var i = null;
	var c = null;
	
    for (b=i=0;c=s.charCodeAt(i++);b+=c>>11?3:c>>7?2:1);
    return b;
};

var getByteEndIndex = function (s, maxByte) {
	var b = null;
	var i = null;
	var c = null;
	
    for (b=i=0;c=s.charCodeAt(i);) {
    	b+=c>>11?3:c>>7?2:1;
    	
    	if (b > maxByte) break;
    	
    	i++;
    }
    
    return i;
};

var check_byte = function (e) {
    var byte = getByteLength(e.target.value);
    if (byte > 50) {
        alert('(' + byte + '/50) 50 byte를 넘길 수 없습니다.');
    }
};

//workbook을 json 배열로 전환한다.
var workbook_to_json = function (workbook, h) {
    var result = {};
    
    workbook.SheetNames.forEach(function (sheetName) {
    	var roa = [];
    	var sheet = workbook.Sheets[sheetName];   
    	
    	if (h) {	
            roa = XLSX.utils.sheet_to_row_object_array(sheet, { range: 1, header: h[sheetName].keys });
        } else {
        	roa = XLSX.utils.sheet_to_row_object_array(sheet);
        }
        
        if (roa.length > 0) {
            result[sheetName] = roa;
        }
    });
    
    return result;
};

//workbook을 배열로 전환한다.
var workbook_to_array = function (workbook) {
    var result = {};
    workbook.SheetNames.forEach(function(sheetName) {
        var roa = XLSX.utils.sheet_to_json(sheet, {header:1});
        
        if (roa.length > 0) {
            result[sheetName] = roa;
        }
    });
    return result;
};

//workbook을 배열로 전환한다.
var workbook_to_sheet = function (workbook) {
    var result = {};
    workbook.SheetNames.forEach(function(sheetName) {
        var roa = XLSX.utils.sheet_to_json(workbook.Sheets[sheetName], {header:'A'});
        if(roa.length > 0){
            result[sheetName] = roa;
        }
    });
    return result;
};

var get_shert_to_headers = function (sheet) {
	var range = XLSX.utils.decode_range(sheet['!ref']);
    var R = range.s.r; /* start in the first row */

    var headers = [];
    
    for (var C = range.s.c; C <= range.e.c; ++C) {
    	var cell = sheet[XLSX.utils.encode_cell({c:C, r:R})];
    	var hdr = "UNKNOWN " + C; // <-- replace with your desired default
        
    	if (cell && cell.t) {
        	hdr = XLSX.utils.format_cell(cell);
        } else {
        	break;
        }
    	
    	headers.push(hdr);
    }
    
    return headers;
}

var read_binary_to_json = function (data, header_info) {
    var deferred = $.Deferred();

    try {
        var workbook = XLSX.read(data, {type: 'binary'});
        
        var h = {};
        
        workbook.SheetNames.forEach(function (sheetName) {
        	var sheet = workbook.Sheets[sheetName];
        	
        	h[sheetName] = {};
    		h[sheetName].headers = get_shert_to_headers(sheet);
    		h[sheetName].keys = h[sheetName].headers.map(function (d, i) {
    			return header_info ? (header_info[d] || d) : d;
    		});
        });
    	
        var l = workbook_to_json(workbook, h);

        // 첫번쨰 시트명을 구해서 array key 값으로 사용 (Sheet 명으로 배열이 생성)
        var sheet_name = Object.keys(l)[0];
        var excel_data = l[sheet_name];
        var excel_headers = h[sheet_name].headers;
        var excel_keys = h[sheet_name].keys;

        if (excel_data) {
            deferred.resolve(excel_data, excel_headers, excel_keys);
        } else {
            deferred.reject('데이터가 없습니다.');
        }
    } catch (e) {
        deferred.reject('엑셀파일 형식이 잘못되었습니다.\n다른 이름으로 저장에서 파일 형식을 xlsx로 지정하시고\n새로운 파일로 저장 후 다시 시도해주세요.');
    }

    return deferred.promise();
};

//엑셀 파일을 읽어 배열로 전환한다.
function read_excel (file, header_info) {
    var deferred = $.Deferred();

    var reader = new FileReader();

    reader.onload = function(e) {
        var binary = e.target.result;

        read_binary_to_json(binary, header_info).then(
            function (data, excel_headers, excel_keys) {
                deferred.resolve(data, excel_headers, excel_keys);
            },
            function (error) {
                deferred.reject(error);
            }
        );
    };

    reader.abort = function (e) {
        deferred.reject('작업이 중단되었습니다. 다시 시도 바랍니다.');
    };

    reader.error = function (e) {
        deferred.reject('에러가 발생하였습니다. 다시 시도 바랍니다.');
    };

    // readAsBinaryString 메서드 익스플로러 미지원으로 예외 코드 작성
    if (reader.readAsBinaryString) {
        reader.readAsBinaryString(file);
    } else {
        var explorer_reader = new FileReader();

        explorer_reader.onload = function (e) {
            var bytes = new Uint8Array(e.target.result);
            var length = bytes.byteLength;
            var binary = '';

            for (var i = 0; i < length; i++) {
                binary += String.fromCharCode(bytes[i]);
            }

            read_binary_to_json(binary, header_info).then(
                function (data, excel_headers, excel_keys) {
                    deferred.resolve(data, excel_headers, excel_keys);
                },
                function (error) {
                    deferred.reject(error);
                }
            );
        };

        explorer_reader.readAsArrayBuffer(file);
    }

    return deferred.promise();
}

var read_excel_to_array = function (file) {
    var deferred = $.Deferred();

    var reader = new FileReader();

    reader.onload = function(e) {
        var data = e.target.result;

        try {
            var workbook = XLSX.read(data, {type: 'binary'});
            var l = workbook_to_array(workbook);

            // 첫번쨰 시트명을 구해서 array key 값으로 사용 (Sheet 명으로 배열이 생성)
            var sheet_name = Object.keys(l)[0];
            var excel_data = l[sheet_name];

            if (excel_data) {
                deferred.resolve(excel_data);
            } else {
                deferred.reject('데이터가 없습니다.');
            }
        } catch (e) {
            deferred.reject('엑셀파일 형식이 잘못되었습니다.\n다른 이름으로 저장에서 파일 형식을 xlsx로 지정하시고\n새로운 파일로 저장 후 다시 시도해주세요.');
        }
    };

    reader.abort = function (e) {
        deferred.reject('작업이 중단되었습니다. 다시 시도 바랍니다.');
    };

    reader.error = function (e) {
        deferred.reject('에러가 발생하였습니다. 다시 시도 바랍니다.');
    };

    reader.readAsBinaryString(file);

    return deferred.promise();
};

var read_excel_to_sheet = function (file) {
    var deferred = $.Deferred();

    var reader = new FileReader();

    reader.onload = function(e) {
        var data = e.target.result;

        try {
            var workbook = XLSX.read(data, {type: 'binary'});
            var l = workbook_to_sheet(workbook);

            // 첫번쨰 시트명을 구해서 array key 값으로 사용 (Sheet 명으로 배열이 생성)
            var sheet_name = Object.keys(l);
            var excel_data = [];

            $.each(sheet_name, function (k, v) {
                for (var i = 0; i < l[v].length; i++) {
                    var excel_json = l[v][i];
                    excel_json.mall_name = v; // 쇼핑몰명 삽입

                    if (i > 2 && excel_json.A) excel_data.push(excel_json);
                }
            });

            if (excel_data) {
                deferred.resolve(excel_data);
            } else {
                deferred.reject('데이터가 없습니다.');
            }
        } catch (e) {
            deferred.reject('엑셀파일 형식이 잘못되었습니다.\n다른 이름으로 저장에서 파일 형식을 xlsx로 지정하시고\n새로운 파일로 저장 후 다시 시도해주세요.');
        }
    };

    reader.abort = function (e) {
        deferred.reject('작업이 중단되었습니다. 다시 시도 바랍니다.');
    };

    reader.error = function (e) {
        deferred.reject('에러가 발생하였습니다. 다시 시도 바랍니다.');
    };

    // readAsBinaryString 메서드 익스플로러 미지원으로 예외 코드 작성
    if (reader.readAsBinaryString) {
        reader.readAsBinaryString(file);
    } else {
        var explorer_reader = new FileReader();

        explorer_reader.onload = function (e) {
            var bytes = new Uint8Array(e.target.result);
            var length = bytes.byteLength;
            var binary = '';

            for (var i = 0; i < length; i++) {
                binary += String.fromCharCode(bytes[i]);
            }

            workbook_to_sheet(binary).then(
                function (data) {
                    deferred.resolve(data);
                },
                function (error) {
                    deferred.reject(error);
                }
            );
        };

        explorer_reader.readAsArrayBuffer(file);
    }

    return deferred.promise();
};

var read_excel_to_headers = function (file) {
    var deferred = $.Deferred();

    var reader = new FileReader();

    reader.onload = function(e) {
        var data = e.target.result;

        try {
            var workbook = XLSX.read(data, {type: 'binary'});
            var l = workbook_to_sheet(workbook);
            
            var h = {};
            
            workbook.SheetNames.forEach(function (sheetName) {
            	var sheet = workbook.Sheets[sheetName];
            	
            	h[sheetName] = get_shert_to_headers(sheet);
            });
        	
            // 첫번쨰 시트명을 구해서 array key 값으로 사용 (Sheet 명으로 배열이 생성)
            var sheet_name = Object.keys(l)[0];

            if (h[sheet_name]) {
                deferred.resolve(h[sheet_name]);
            } else {
                deferred.reject('데이터가 없습니다.');
            }
        } catch (e) {
            deferred.reject('엑셀파일 형식이 잘못되었습니다.\n다른 이름으로 저장에서 파일 형식을 xlsx로 지정하시고\n새로운 파일로 저장 후 다시 시도해주세요.');
        }
    };

    reader.abort = function (e) {
        deferred.reject('작업이 중단되었습니다. 다시 시도 바랍니다.');
    };

    reader.error = function (e) {
        deferred.reject('에러가 발생하였습니다. 다시 시도 바랍니다.');
    };

    // readAsBinaryString 메서드 익스플로러 미지원으로 예외 코드 작성
    if (reader.readAsBinaryString) {
        reader.readAsBinaryString(file);
    } else {
        var explorer_reader = new FileReader();

        explorer_reader.onload = function (e) {
            var bytes = new Uint8Array(e.target.result);
            var length = bytes.byteLength;
            var binary = '';

            for (var i = 0; i < length; i++) {
                binary += String.fromCharCode(bytes[i]);
            }

            workbook_to_sheet(binary).then(
                function (data) {
                    deferred.resolve(data);
                },
                function (error) {
                    deferred.reject(error);
                }
            );
        };

        explorer_reader.readAsArrayBuffer(file);
    }

    return deferred.promise();
};

var download_products = function (download_data, filename, with_mall_product_ids) {
    var deferred = $.Deferred();

    var sql =
        "SELECT id [(필수) 상품코드], category_kind [카테고리], " +
        "       IF(category_id = 0, '', category_id) [카테고리ID], " +
        "       product_name [(필수) 상품명], " +
        "       short_name [상품약어], " +
        "       keyword [검색어], " +
        "       english_name [영문상품명], " +
        "       chinese_name [중문상품명], " +
        "       purchase_product_name [매입상품명], " +
        "       own_code [자사상품코드], brand [브랜드명], model [모델명], model_no [모델번호], " +
        "       CASE WHEN sale_status = 2000 THEN '1' " +
        "            WHEN sale_status = 2001 THEN '2' " +
        "            WHEN sale_status = 2002 THEN '3' " +
        "            WHEN sale_status = 2003 THEN '4' " +
        "            WHEN sale_status = 2004 THEN '5' " +
        "            WHEN sale_status = 2005 THEN '6' " +
        "            WHEN sale_status = 2006 THEN '7' " +
        "            ELSE '' " +
        "       END as [(필수) 판매상태], " +
        "       IFNULL(supplier_name, '') [매입처], " +
        "       manufacturer [(필수) 제조사], " +
        "       origin_name [(필수) 원산지], " +
        "       CASE WHEN gender = 1281 THEN '1' " +
        "            WHEN gender = 1282 THEN '2' " +
        "            WHEN gender = 1283 THEN '3' " +
        "            WHEN gender = 1284 THEN '4' " +
        "            WHEN gender = 1285 THEN '5' " +
        "            ELSE '' " +
        "       END as [남녀정보], " +
        "       CASE WHEN sale_area = 1277 THEN '1' " +
        "            WHEN sale_area = 1278 THEN '2' " +
        "            WHEN sale_area = 1279 THEN '3' " +
        "            WHEN sale_area = 1280 THEN '4' " +
        "            ELSE '' " +
        "       END as [판매지역정보], " +
        "       CASE WHEN season = 1290 THEN '1' " +
        "            WHEN season = 1291 THEN '2' " +
        "            WHEN season = 1292 THEN '3' " +
        "            WHEN season = 1293 THEN '4' " +
        "            WHEN season = 1294 THEN '5' " +
        "            WHEN season = 1295 THEN '6' " +
        "            ELSE '' " +
        "       END as [시즌정보], " +
        "       CASE WHEN delivery_method = 1296 THEN '1' " +
        "            WHEN delivery_method = 1297 THEN '2' " +
        "            WHEN delivery_method = 1298 THEN '3' " +
        "            WHEN delivery_method = 1299 THEN '4' " +
        "            WHEN delivery_method = 1300 THEN '5' " +
        "            WHEN delivery_method = 1301 THEN '6' " +
        "            ELSE '' " +
        "       END as [배송비정보], " +
        "       CASE WHEN tax = 1286 THEN '1' " +
        "            WHEN tax = 1287 THEN '2' " +
        "            WHEN tax = 1288 THEN '3' " +
        "            WHEN tax = 1289 THEN '4' " +
        "            ELSE '' " +
        "       END as [(필수) 과세정보], " +
        "       delivery_fee [배송비], " +
        "       food_origin [식품재료/원산지], " +
        "       IF(manufacture_at = 0, '', manufacture_at) [제조년도], " +
        "       IF(issue_date = 0, '', issue_date) [발행일(제조일)], " +
        "       IF(expiry_at = 0, '', expiry_at) [유효기간], " +
        "       IF(agro_fishery = 0, '', agro_fishery) [농수산물구분], " +
        "       IF(kg = 0, '', weight) [상품전체중량], " +
        "       CASE WHEN certify_assort = 1380 THEN 'A' " + // A : 친환경
        "            WHEN certify_assort = 1381 THEN 'B' " + // B : 생활제품
        "            WHEN certify_assort = 1382 THEN 'C' " + // C : 전기용품
        "            WHEN certify_assort = 1383 THEN 'D' " + // D : 방송통신기자재
        "            WHEN certify_assort = 1384 THEN 'E' " + // E : 어린이제품
        "            WHEN certify_assort = 1385 THEN 'F' " + // F : 자가검사번호
        "            WHEN certify_assort = 1386 THEN 'G' " + // G : 위해요소중점관리인증(HACCP)
        "            WHEN certify_assort = 1387 THEN 'H' " + // H : 농산물우수관리인증(GAP)
        "            WHEN certify_assort = 1388 THEN 'I' " + // I : 가공식품표준화인증(KS)
        "            WHEN certify_assort = 1389 THEN 'J' " + // J : 우수건강식품제조기준인증(GMP)
        "            WHEN certify_assort = 1390 THEN 'K' " + // K : 건강기능식품인증
        "            WHEN certify_assort = 1391 THEN 'L' " + // L : 수산물품질인증
        "            WHEN certify_assort = 1392 THEN 'M' " + // M : 수산특산물품질인증
        "            WHEN certify_assort = 1393 THEN 'N' " + // N : 수산전통식품품질인증
        "            WHEN certify_assort = 1394 THEN 'O' " + // O : 전통식품품질인증
        "            WHEN certify_assort = 1395 THEN 'P' " + // P : 유기가공식품인증
        "            WHEN certify_assort = 1396 THEN 'Q' " + // Q : 원산지증명
        "            WHEN certify_assort = 1397 THEN 'R' " + // R : 농산물이력추적
        "            WHEN certify_assort = 1398 THEN 'S' " + // S : 도축증명서
        "            WHEN certify_assort = 1399 THEN 'T' " + // T : 등급판정확인서
        "            WHEN certify_assort = 1400 THEN 'U' " + // U : 형식승인
        "            WHEN certify_assort = 1401 THEN 'V' " + // V : 형식검정/형식등록
        "            WHEN certify_assort = 1402 THEN 'W' " + // W : 해외안전인증서류
        "            WHEN certify_assort = 1527 THEN 'X' " + // X : 상세설명참조
        "            ELSE '' " +
        "       END as [인증대상구분], " +
            //친환경
        "       CASE WHEN certify_assort = 1380 AND certify_item = 1404 THEN '1' " +
        "            WHEN certify_assort = 1380 AND certify_item = 1405 THEN '2' " +
        "            WHEN certify_assort = 1380 AND certify_item = 1406 THEN '3' " +
        "            WHEN certify_assort = 1380 AND certify_item = 1407 THEN '4' " +
        "            WHEN certify_assort = 1380 AND certify_item = 1408 THEN '5' " +
        "            WHEN certify_assort = 1380 AND certify_item = 1409 THEN '6' " +
        "            WHEN certify_assort = 1380 AND certify_item = 1410 THEN '7' " +
            //생활제품
        "            WHEN certify_assort = 1381 AND certify_item = 1411 THEN '1' " +
        "            WHEN certify_assort = 1381 AND certify_item = 1412 THEN '2' " +
        "            WHEN certify_assort = 1381 AND certify_item = 1413 THEN '3' " +
        "            WHEN certify_assort = 1381 AND certify_item = 1414 THEN '4' " +
            //전기용품
        "            WHEN certify_assort = 1382 AND certify_item = 1415 THEN '1' " +
        "            WHEN certify_assort = 1382 AND certify_item = 1416 THEN '2' " +
        "            WHEN certify_assort = 1382 AND certify_item = 1417 THEN '3' " +
            //방송통신기자재
        "            WHEN certify_assort = 1383 AND certify_item = 1418 THEN '1' " +
        "            WHEN certify_assort = 1383 AND certify_item = 1419 THEN '2' " +
        "            WHEN certify_assort = 1383 AND certify_item = 1420 THEN '3' " +
            //어린이제품
        "            WHEN certify_assort = 1384 AND certify_item = 1421 THEN '1' " +
        "            WHEN certify_assort = 1384 AND certify_item = 1422 THEN '2' " +
        "            WHEN certify_assort = 1384 AND certify_item = 1423 THEN '3' " +
        "            ELSE '' " +
        "       END as [인증대상품목], " +
        "       certify_doc_no [인증문서번호], " +
        "       CASE WHEN certify_organization = 1424 THEN '1' " + // 1 : KOTITI시험연구원
        "            WHEN certify_organization = 1425 THEN '2' " + // 2 : 산업기술시험원
        "            WHEN certify_organization = 1426 THEN '3' " + // 3 : FITI 시험연구원
        "            WHEN certify_organization = 1427 THEN '4' " + // 4 : 한국의류시험연구원
        "            WHEN certify_organization = 1428 THEN '5' " + // 5 : 한국기계전기전자시험연원
        "            WHEN certify_organization = 1429 THEN '6' " + // 6 : 한국건설생활환경시험연원
        "            WHEN certify_organization = 1430 THEN '7' " + // 7 : 한국화학융합시험연구원
        "            ELSE '' " +
        "       END as [인증기관], " +
        "       IF(certify_issue_date = 0, '', certify_issue_date) [발급일자], " +
        "       IF(certify_date = 0, '', certify_date) [인증일자], " +
        "       IF(certify_start_date = 0, '', certify_start_date) [인증기간(시작일)], " +
        "       IF(certify_end_date = 0, '', certify_end_date) [인증기간(종료일)], " +
        "       IFNULL(image_url100, '') [인증문서이미지], " +
        "       CASE WHEN certify_report = 1431 THEN '1' " + // 1 : 식품제조/가공업
        "            WHEN certify_report = 1432 THEN '2' " + // 2 : 기능식품
        "            WHEN certify_report = 1433 THEN '3' " + // 3 : 의료기기
        "            ELSE '' " +
        "       END as [신고대상], " +
        "       certify_report_org [신고기관], certify_report_no [신고번호], certify_permit_no [품목허가번호], " +
        "       CASE WHEN certify_consider_yn = 1 THEN 'Y' " +
        "            WHEN certify_consider_yn = 2 THEN 'N' " +
        "            ELSE '' " +
        "       END as [사전광고심의유무], " +
        "       certify_consider_no [사전광고심의번호], market_price [소비자가], sale_price [(필수) 판매가], supply_price [공급가(원가)], " +
        "       supply_commission [공급가 수수료], buyer_price [매입가(원가)], buyer_commission [매입가수수료], " +
        "       IFNULL(option_name1, '') [옵션명1], IFNULL(option_value1, '') [옵션상세1], " +
        "       IFNULL(option_name2, '') [옵션명2], IFNULL(option_value2, '') [옵션상세2], " +
        "       IFNULL(option_name3, '') [옵션명3], IFNULL(option_value3, '') [옵션상세3], " +
        "       short_note [상품간략 설명], add_note [상품추가 설명1], add_note2 [상품추가 설명2], head_note [상세설명 상단], " +
        "       detail_note [(필수) 상세설명], tail_note [상세설명 하단], " +
        "       IFNULL(image_url1, '') [(필수) 대표이미지], " +
        "       IFNULL(image_url2, '') [11번가 목록이미지], " +
        "       IFNULL(image_url3, '') [리스트이미지], " +
        "       IFNULL(image_url4, '') [W컨셉 대표이미지], " +
        "       IFNULL(image_url5, '') [WIZWID 대표이미지], " +
        "       IFNULL(image_url6, '') [무신사 대표이미지], " +
        "       IFNULL(image_url41, '') [OCO 대표이미지], " +
        "       IFNULL(image_url42, '') [이지웰 모바일이미지], " +
        "       IFNULL(image_url7, '') [부가이미지7], " +
        "       IFNULL(image_url8, '') [부가이미지8], " +
        "       IFNULL(image_url9, '') [부가이미지9], " +
        "       IFNULL(image_url10, '') [부가이미지10], " +
        "       IFNULL(image_url11, '') [부가이미지11], " +
        "       IFNULL(image_url12, '') [부가이미지12], " +
        "       IFNULL(image_url13, '') [부가이미지13], " +
        "       IFNULL(image_url14, '') [부가이미지14], " +
        "       IFNULL(image_url15, '') [부가이미지15], " +
        "       IFNULL(image_url16, '') [부가이미지16], " +
        "       IFNULL(image_url17, '') [부가이미지17], " +
        "       IFNULL(image_url18, '') [부가이미지18], " +
        "       IFNULL(image_url19, '') [부가이미지19], " +
        "       IFNULL(image_url20, '') [부가이미지20], " +
        "       IFNULL(image_url21, '') [부가이미지21], " +
        "       IFNULL(image_url22, '') [부가이미지22], " +
        "       IFNULL(image_url28, '') [부가이미지23], " +
        "       IFNULL(image_url29, '') [부가이미지24], " +
        "       IFNULL(image_url30, '') [부가이미지25], " +
        "       IFNULL(image_url31, '') [부가이미지26], " +
        "       IFNULL(image_url32, '') [부가이미지27], " +
        "       IFNULL(image_url33, '') [부가이미지28], " +
        "       IFNULL(image_url34, '') [부가이미지29], " +
        "       IFNULL(image_url35, '') [부가이미지30], " +
        "       IFNULL(image_url36, '') [부가이미지31], " +
        "       IFNULL(image_url37, '') [부가이미지32], " +
        "       IFNULL(image_url38, '') [부가이미지33], " +
        "       IFNULL(image_url39, '') [부가이미지34], " +
        "       IFNULL(image_url40, '') [부가이미지35], " +
        "       notify_code [정보고시분류코드], " +
        "       IFNULL(notify1, '') [정보고시상세1], " +
        "       IFNULL(notify2, '') [정보고시상세2], " +
        "       IFNULL(notify3, '') [정보고시상세3], " +
        "       IFNULL(notify4, '') [정보고시상세4], " +
        "       IFNULL(notify5, '') [정보고시상세5], " +
        "       IFNULL(notify6, '') [정보고시상세6], " +
        "       IFNULL(notify7, '') [정보고시상세7], " +
        "       IFNULL(notify8, '') [정보고시상세8], " +
        "       IFNULL(notify9, '') [정보고시상세9], " +
        "       IFNULL(notify10, '') [정보고시상세10], " +
        "       IFNULL(notify11, '') [정보고시상세11], " +
        "       IFNULL(notify12, '') [정보고시상세12], " +
        "       IFNULL(notify13, '') [정보고시상세13], " +
        "       IFNULL(notify14, '') [정보고시상세14], " +
        "       IFNULL(notify15, '') [정보고시상세15], " +
        "       IFNULL(color, 0) [색상], " +
        "       IFNULL(logistic_name, '') [물류처], " +
        "       IFNULL(image_url200, '') [품질검사(QA)1], " +
        "       IFNULL(image_url201, '') [품질검사(QA)2], " +
        "       IFNULL(image_url202, '') [품질검사(QA)3], " +
        "       IFNULL(image_url203, '') [품질검사(QA)4], " +
        "       IFNULL(image_url204, '') [품질검사(QA)5], " +
        "       IFNULL(image_url205, '') [품질검사(QA)6], " +
        "       IFNULL(image_url206, '') [품질검사(QA)7], " +
        "       IFNULL(stock_cnt, 0) [실재고계] ";

    if (with_mall_product_ids) {
        get_malls().then(
            function (malls) {
                var mall_product_ids_query = malls.map(function (mall) {
                    return "IFNULL(mall_product_id_" + mall.id + ", '') [" + mall.mall_name + "]"
                }).join(', ');

                sql +=
                    (mall_product_ids_query.length == 0 ? "" : ", " + mall_product_ids_query) +
                    "  INTO XLSX('" + filename + ".xlsx', {headers: true}) " +
                    "  FROM ? ";

                alasql.promise(
                    sql,
                    [download_data]
                ).then(
                    function (data) {
                        deferred.resolve();
                    }
                ).catch(
                    function (err) {
                        alert("저장에 실패하였습니다.");
                        deferred.reject();
                    }
                );
            },
            function () {
                alert("저장에 실패하였습니다.");
                deferred.reject();
            });
    } else {
        sql +=
            "  INTO XLSX('" + filename + ".xlsx', {headers: true}) " +
            "  FROM ? ";

        alasql.promise(
            sql,
            [download_data]
        ).then(
            function (data) {
                deferred.resolve();
            }
        ).catch(
            function (err) {
                alert("저장에 실패하였습니다.");
                deferred.reject();
            }
        );
    }

    return deferred.promise();
};

var download_product_stocks = function (download_data, filename) {
    var deferred = $.Deferred();

    alasql.promise(
        " SELECT id [옵션ID(변경불가)], " +
        "        OPTION_CODE_FORMAT_STRING(product_id, option_code) [옵션코드(변경불가)], " +
        "        product_name [상품명(변경불가)], " +
        "        option_item [옵션상세(변경불가)], " +
        "        IFNULL(stock_barcode, '') [바코드], " +
        "        stock_name [옵션별칭], " +
        "        stock_code [재고코드], " +
        "        TO_INT(present_stock) [실재고(변경불가)], " +
        "        TO_INT(automatic_stock) [자동재고], " +
        "        TO_INT(safety_stock) [안전재고], " +
        "        TO_INT(virtual_stock) [임의재고], " +
        "        TO_INT(bundle_amount) [묶음수량], " +
        "        IFNULL(option_sale_status_name, '') [판매상태(변경불가)], " +
        "        TO_INT(supply_price) [공급가(변경불가)], " +
        "        TO_INT(sale_price) [판매가(변경불가)], " +
        "        TO_INT(add_price) [추가금액], " +
        "        DATE_FORMAT(created_at, 'YYYY-MM-DD HH:mm:ss') [입력일(변경불가)], " +
        "        DATE_FORMAT(updated_at, 'YYYY-MM-DD HH:mm:ss') [수정일(변경불가)], " +
        "        IFNULL(own_code, '') [자사상품코드(변경불가)], " +
        "        IFNULL(model, '') [모델명(변경불가)], " +
        "        IFNULL(model_no, '') [모델번호(변경불가)] " +
        "  INTO XLSX('" + filename + ".xlsx', {headers: true}) " +
        "   FROM ? a " +
        "  ORDER BY product_id, option_code ASC ",
        [download_data]
    ).then(
        function (data) {
            deferred.resolve();
        }
    ).catch(
        function (err) {
            alert("저장에 실패하였습니다.");
            deferred.reject();
        }
    );

    return deferred.promise();
};

var download_product_stock_set = function (download_data, filename) {
    var deferred = $.Deferred();

    alasql.promise(
        " SELECT id [옵션ID(변경불가)], " +
        "        OPTION_CODE_FORMAT_STRING(product_id, option_code) [옵션코드(변경불가)], " +
        "        product_name [상품명(변경불가)], " +
        "        option_item [옵션상세(변경불가)], " +
        "        IFNULL(stock_set_id, '') [세트ID(변경불가)], " +
        "        CASE WHEN setting_product_id IS NOT NULL THEN OPTION_CODE_FORMAT_STRING(setting_product_id, setting_option_code) ELSE '' END [세트옵션코드], " +
        "        IFNULL(setting_product_name, '') [세트상품명(변경불가)], " +
        "        IFNULL(setting_option_item, '') [세트옵션상세(변경불가)], " +
        "        IFNULL(setting_sale_price, '') [세트옵션판매가] " +
        "  INTO XLSX('" + filename + ".xlsx', {headers: true}) " +
        "   FROM ? a " +
        "  ORDER BY product_id, option_code ASC ",
        [download_data]
    ).then(
        function (data) {
            deferred.resolve();
        }
    ).catch(
        function (err) {
            alert("저장에 실패하였습니다.");
            deferred.reject();
        }
    );

    return deferred.promise();
};

var download_product_notifies = function (assort_code, download_data, filename) {
    var deferred = $.Deferred();

    $.ajax({
        type: 'POST',
        url: '/common/get_goods_notifies',
        data: {
            assort_code: assort_code
        },
        success: function (data) {
            var field_names = data.map(function (field, index) {
                return 'IFNULL(field' + (index+1) + ', \'\') [' + field.item_name + ']';
            }).join(', ');

            if (field_names.length > 0) {
                field_names = ', ' + field_names;
            }

            alasql.promise(
                " SELECT mall_id [쇼핑몰코드], " +
                "        id [상품코드(변경불가)], " +
                "        own_code [자사상품코드(변경불가)], " +
                "        product_name [상품명(변경불가)], " +
                "        sale_status_name [판매상태(변경불가)], " +
                assort_code + " [상품정보고시] " +
                field_names +
                "   INTO XLSX('" + filename + ".xlsx', {headers: true}) " +
                "   FROM ? ",
                [download_data]
            ).then(
                function (data) {
                    deferred.resolve();
                }
            ).catch(
                function (err) {
                    alert("저장에 실패하였습니다.");
                    deferred.reject();
                }
            );
        }
    });

    return deferred.promise();
};

var download_linkage_product_names = function (download_data, filename) {
    var deferred = $.Deferred();

    alasql.promise(
        " SELECT mall_id [쇼핑몰코드], " +
        "        id [상품코드(변경불가)], " +
        "        IFNULL(own_code, '') [자사상품코드(변경불가)], " +
        "        IFNULL(product_name, '') [상품명], " +
        "        sale_status_name [판매상태(변경불가)], " +
        "        IFNULL(supplier_name, '') [매입처(변경불가)] " +
        "   INTO XLSX('" + filename + ".xlsx', {headers: true}) " +
        "   FROM ? ",
        [download_data]
    ).then(
        function (data) {
            deferred.resolve();
        }
    ).catch(
        function (err) {
            alert("저장에 실패하였습니다.");
            deferred.reject();
        }
    );

    return deferred.promise();
};

var download_linkage_prices = function (download_data, filename) {
    var deferred = $.Deferred();

    alasql.promise(
        " SELECT mall_id [쇼핑몰코드], " +
        "        id [상품코드(변경불가)], " +
        "        own_code [자사상품코드(변경불가)], " +
        "        product_name [상품명(변경불가)], " +
        "        sale_status_name [판매상태(변경불가)], " +
        "        IFNULL(supplier_name, '') [매입처(변경불가)], " +
        "        market_price [소비자가], " +
        "        sale_price [판매가], " +
        "        supply_price [공급가], " +
        "        commission [수수료] " +
        "   INTO XLSX('" + filename + ".xlsx', {headers: true}) " +
        "   FROM ? ",
        [download_data]
    ).then(
        function (data) {
            deferred.resolve();
        }
    ).catch(
        function (err) {
            alert("저장에 실패하였습니다.");
            deferred.reject();
        }
    );

    return deferred.promise();
};

// 쇼핑몰별상세설명
var download_linkage_detail_notes = function (download_data, filename) {
    var deferred = $.Deferred();

    alasql.promise(
        " SELECT IIF(mall_id=0, '', mall_id) [쇼핑몰코드], " +
        "        id [상품코드(변경불가)], " +
        "        IFNULL(own_code, '') [자사상품코드(변경불가)], " +
        "        IFNULL(head_note, '') [상세설명 상단], " +
        "        IFNULL(detail_note, '') [상세설명], " +
        "        IFNULL(tail_note, '') [상세설명 하단], " +
        "        sale_status_name [판매상태(변경불가)], " +
        "        IFNULL(supplier_name, '') [매입처(변경불가)] " +
        "   INTO XLSX('" + filename + ".xlsx', {headers: true}) " +
        "   FROM ? ",
        [download_data]
    ).then(
        function (data) {
            deferred.resolve();
        }
    ).catch(
        function (err) {
            alert("저장에 실패하였습니다.");
            deferred.reject();
        }
    );

    return deferred.promise();
};

var download_orders = function (excel_form_id, download_data, filename) {
    var deferred = $.Deferred();

    $.ajax({
    	type: 'POST',
        url: '/common/get_order_down_excel_form',
        data: {
            id: excel_form_id
        },
        success: function (data) {
            var excel_fields = alasql("SELECT INDEX id, field_name FROM ? ", [data.fields]);
            var excel_form = data.excel_form;

            var fields = [];

            var opts = {
                headers: true
            };

            excel_form.field_value = JSON.parse(excel_form.field_value);

            $.each(excel_form.field_value, function (k, v) {
                var id = parseInt(v.field);
                var field_name = excel_fields[id];

                if (field_name == 'blank') {
                    fields.push("''" + " " + "[" + v.title + "]");
                } else if (field_name == 'comment') {
                    fields.push("'" + v.text + "'" + " " + "[" + v.title + "]");
                } else {
                    fields.push(field_name + " " + "[" + v.title + "]");
                }
            });

            if (excel_form.packing > 0) {
                var packing_fields = Face2Init.packings[excel_form.packing].fields; //합포장 필드
                var field_string = packing_fields.join(); //alasql에서 사용할 필드 string 값

                //합포장 주문 필터
                var packing_list = alasql(
                    " SELECT " + field_string +
                    "   FROM ? " +
                    " GROUP BY " + field_string +
                    " HAVING COUNT(*) > 1",
                    [download_data]
                );

                //합포장 필드로 소팅
                download_data = alasql("SELECT * FROM ? ORDER BY " + packing_fields.join(), [download_data]);

                //합포장 주문 스타일 입히기
                alasql(
                    "SELECT " + fields.join() + ', IF(b.' + packing_fields[0] + ', 1, 0) packing' +
                    "  FROM ? a " +
                    "       LEFT JOIN ? b " +
                    "       ON " +
                    packing_fields.map(function (value) {
                        return 'a.' + value + ' = b.' + value;
                    }).join(' AND '),
                    [download_data, packing_list]
                ).forEach(
                    function (v, i) {
                        if (v.packing == 0) return;

                        if (!opts.rows) {
                            opts.rows = {};
                        }

                        opts.rows[i] = {
                            style: {
                                Interior: {
                                    Color: '#c7e2f5',
                                    Pattern: 'Solid'
                                }
                            }
                        };
                    }
                );

                //엑셀 출력하기
                alasql.promise(
                    "SELECT " + fields.join() +
                    "  INTO XLSXML('" + filename + ".xls', ?) " +
                    "  FROM ? ",
                    [opts, download_data]
                ).then(
                    function () {
                        deferred.resolve();
                    }
                ).catch(
                    function (err) {
                        alert("저장에 실패하였습니다.");
                        deferred.reject();
                    }
                );
            } else {

                //엑셀 출력하기
                alasql.promise(
                    "SELECT " + fields.join() +
                    "  INTO XLSX('" + filename + ".xlsx', ?) " +
                    "  FROM ? ",
                    [opts, download_data]
                ).then(
                    function () {
                        deferred.resolve();
                    }
                ).catch(
                    function (err) {
                        alert("저장에 실패하였습니다.");
                        deferred.reject();
                    }
                );
            }
        }
    });

    return deferred.promise();
};

// 티몰 배송정보 다운로드
var download_tmall_orders = function (download_data) {
    var deferred = $.Deferred();
    var idx = 0;

    try {
        $.each(download_data, function (k, v) {
            if (v.mall_id == 1098 && v.delivery_etc2) {
                // 다운로드 파일명(주문번호 + 송장번호)
                var download_file_name = v.id + '_' + v.invoice;

                setTimeout(function() {
                    pdf_download(v.delivery_etc2, download_file_name);
                }, idx * 1000);

                idx += 1;
            }
        });

        deferred.resolve();
    } catch (e) {
        alert("저장에 실패하였습니다.");
        deferred.reject();
    }

    return deferred.promise();
};

var download_product_matches = function (download_data, filename) {
    var deferred = $.Deferred();

    alasql.promise(
        "SELECT mall_name [쇼핑몰명], " +
        "       mall_product_id [쇼핑몰상품코드], " +
        "       order_option [쇼핑몰옵션명], " +
        "       product_id [상품코드], " +
        "       OPTION_CODE_FORMAT_STRING(product_id, option_code) [옵션코드], " +
        "       option_item [옵션명] " +
        "  INTO XLSX('" + filename + ".xlsx', {headers: true}) " +
        "  FROM ? ",
        [download_data]
    ).then(
        function (data) {
            deferred.resolve();
        }
    ).catch(
        function (err) {
            alert("저장에 실패하였습니다.");
            deferred.reject();
        }
    );

    return deferred.promise();
};

var download_expected_order_options = function (download_data, filename) {
    var deferred = $.Deferred();

    alasql.promise(
        "SELECT mall_name [쇼핑몰명], " +
        "       mall_product_id [쇼핑몰상품코드], " +
        "       GET_MALL_OPTION_FORMAT_STRING(mall_id, option_name, option_item) [쇼핑몰옵션명], " +
        "       product_id [상품코드], " +
        "       OPTION_CODE_FORMAT_STRING(product_id, option_code) [옵션코드], " +
        "       option_item [옵션명] " +
        "  INTO XLSX('" + filename + ".xlsx', {headers: true}) " +
        "  FROM ? ",
        [download_data]
    ).then(
        function (data) {
            deferred.resolve();
        }
    ).catch(
        function (err) {
            alert("저장에 실패하였습니다.");
            deferred.reject();
        }
    );

    return deferred.promise();
};

// 쇼핑몰정산금액비교-정산엑셀저장
var download_adjustment_compare = function (download_data, filename) {
    var deferred = $.Deferred();

    alasql.promise(
        "SELECT id [주문번호(변경불가)], " +
        "       reflection_amount [정산금액] " +
        "  INTO XLSX('" + filename + ".xlsx', {headers: true}) " +
        "  FROM ? ",
        [download_data]
    ).then(
        function (data) {
            deferred.resolve();
        }
    ).catch(
        function (err) {
            alert("저장에 실패하였습니다.");
            deferred.reject();
        }
    );

    return deferred.promise();
};

var download_erp_stocks = function (download_data, filename) {
    var deferred = $.Deferred();
    var sql =
        " SELECT product_id [상품코드], " +
        "        own_code [자사상품코드], " +
        "        product_name [상품명], " +
        "        OPTION_CODE_FORMAT_STRING(product_id, option_code) [옵션코드], " +
        "        option_item [옵션상세], " +
        "        IFNULL(stock_barcode, '') [바코드], ";

    if ($('#CUSTOMER_SETTING_01011001').val() == '0') {
        sql +=
            "        TO_INT(present_stock) [실재고], " +
            "        TO_INT(no_shipping_quantity) [미접수수량], " +
            "        TO_INT(present_stock-no_shipping_quantity) [실재고-미발송수량] ";
    } else if ($('#CUSTOMER_SETTING_01011001').val() == '1') {
        sql +=
            "        TO_INT(warehouse_present_stock) [실재고(재고관리)], " +
            "        TO_INT(no_shipping_quantity) [미접수수량], " +
            "        TO_INT(warehouse_present_stock-no_shipping_quantity) [실재고(재고관리)-미발송수량] ";
    } else if ($('#CUSTOMER_SETTING_01011001').val() == '2') {
        sql +=
            "        IFNULL(style_no, '') [품번(ERP)], " +
            "        IFNULL(color_code, '') [색상(ERP)], " +
            "        IFNULL(size_code, '') [사이즈(ERP)], " +
            "        IFNULL(style_name, '') [품번명(ERP)], " +
            "        IFNULL(color_name, '') [색상명(ERP)], " +
            "        IFNULL(size_name, '') [사이즈명(ERP)], " +
            "        product_status [상품상태(ERP)], " +
            "        TO_INT(use_stock) [가능재고(ERP)], " +
            "        TO_INT(no_shipping_quantity) [미접수수량], " +
            "        TO_INT(use_stock-no_shipping_quantity) [가능재고(ERP)-미접수수량] ";
    }

    sql +=
        "  INTO XLSX('" + filename + ".xlsx', {headers: true}) " +
        "   FROM ? ";

    alasql.promise(
        sql,
        [download_data, Face2Init.product_statuses]
    ).then(
        function (data) {
            deferred.resolve();
        }
    ).catch(
        function (err) {
            alert("저장에 실패하였습니다.");
            deferred.reject();
        }
    );

    return deferred.promise();
};

var form_summit = function (method, target, action, post_data) {
    var form = document.getElementById('form');

    //form 내에 element 제거
    for (var i=form.childNodes.length-1; i>=0; i--) form.removeChild(form.childNodes[i]);

    form.method = method;
    form.target = target;
    form.action = getBaseUrl() + action;

    var input = null;

    if (post_data) {
        for (var key in post_data) {
        	if (Array.isArray(post_data[key])) {
        		for (var i=0; i<post_data[key].length; i++) {
        			if (Array.isArray(post_data[key][i])) {
        				for (j=0; j<post_data[key][i].length; j++) {
        					input = document.createElement("input");
                            input.type = "hidden";
                            input.name = key + '[][]';
                            input.value = post_data[key][i][j];
                            form.appendChild(input);
        				}
        			} else {
        				input = document.createElement("input");
                        input.type = "hidden";
                        input.name = key + '[]';
                        input.value = post_data[key][i];
                        form.appendChild(input);
        			}
        		}
        	} else {
        		if (post_data[key] != null) {
        			input = document.createElement("input");
                    input.type = "hidden";
                    input.name = key;

                    input.value = typeof post_data[key] === "object" ? JSON.stringify(post_data[key]) : post_data[key];

                    form.appendChild(input);
        		}
        	}
        }
    }

    form.submit();
};

var href_menu = function (path, menu_id) {
	var post_data = {
		menu_id: menu_id
	};
	
	var pathArray = location.href.split('/');
	var protocol = pathArray[0];
	var domain = pathArray[2];
	
	var url = protocol + '//' + domain;
	
	var form = document.getElementById('form');

    //form 내에 element 제거
    for (var i=form.childNodes.length-1; i>=0; i--) form.removeChild(form.childNodes[i]);

    form.method = 'POST';
    form.target = '_self';
    form.action = url + path;

    var input = null;

    if (post_data) {
        for (var key in post_data) {
        	if (Array.isArray(post_data[key])) {
        		for (var i=0; i<post_data[key].length; i++) {
        			if (Array.isArray(post_data[key][i])) {
        				for (j=0; j<post_data[key][i].length; j++) {
        					input = document.createElement("input");
                            input.type = "hidden";
                            input.name = key + '[][]';
                            input.value = post_data[key][i][j];
                            form.appendChild(input);
        				}
        			} else {
        				input = document.createElement("input");
                        input.type = "hidden";
                        input.name = key + '[]';
                        input.value = post_data[key][i];
                        form.appendChild(input);
        			}
        		}
        	} else {
        		if (post_data[key] != null) {
        			input = document.createElement("input");
                    input.type = "hidden";
                    input.name = key;

                    input.value = typeof post_data[key] === "object" ? JSON.stringify(post_data[key]) : post_data[key];

                    form.appendChild(input);
        		}
        	}
        }
    }

    form.submit();
};

var main_open_window = function (target, url, post_data, width, height, margin) {
    var options = [];

    // 듀얼모니터 사이즈 계산
    var popWidth  = width; // 파업사이즈 너비
    var popHeight = height; // 팝업사이즈 높이

    var winWidth  = document.body.clientWidth;  // 현재창의 너비
    var winHeight = document.body.clientHeight; // 현재창의 높이
    var winX      = window.screenX || window.screenLeft || 0;// 현재창의 x좌표
    var winY      = window.screenY || window.screenTop || 0; // 현재창의 y좌표

    left_margin = winX + (winWidth - popWidth - margin) / 2;
    top_margin = winY + (winHeight - popHeight) / 2;

    if (width && height) {
        options.push('left=' + left_margin);
        options.push('top=' + top_margin);
    }

    if (width) {
        options.push('width=' + width.toString());
    }

    if (height) {
        options.push('height=' + height.toString());
    }

    if (options.length > 0) {
        window.open('', target, options.join(','));
    } else {
        window.open('', target);
    }

    form_summit('POST', target, url, post_data);
};

var open_window = function (target, url, post_data, width, height, method) {
    var options = ["resizable=yes", "scrollbars=yes"];

    // 듀얼모니터 사이즈 계산
    var popWidth  = width; // 파업사이즈 너비
    var popHeight = height; // 팝업사이즈 높이

    var winWidth  = document.body.clientWidth;  // 현재창의 너비
    var winHeight = document.body.clientHeight; // 현재창의 높이
    var winX      = window.screenX || window.screenLeft || 0;// 현재창의 x좌표
    var winY      = window.screenY || window.screenTop || 0; // 현재창의 y좌표
    left_margin = winX + (winWidth - popWidth) / 2;
    top_margin = winY + (winHeight - popHeight) / 2;

    if (width && height) {
        options.push('left=' + left_margin);
        options.push('top=' + top_margin);
    }

    if (width) {
        options.push('width=' + width.toString());
    }

    if (height) {
        options.push('height=' + height.toString());
    }

    if (options.length > 0) {
    	window.open('', target, options.join(','));
    } else {
    	window.open('', target);
    }

    if (method) {
        form_summit(method, target, url, post_data);
    } else {
        form_summit('POST', target, url, post_data);
    }
};

var href_page = function (url, post_data) {
    form_summit('POST', '_self', url, post_data);
};

var href_blank = function (url, post_data) {
    form_summit('GET', '_blank', url, post_data);
};

var filedownload = function (url, params, successCallback) {
	if (!successCallback) {
		successCallback = function (url) {
			// 로딩 화면 숨기기 처리
		};
	}
	
	$.fileDownload(getBaseUrl() + url, {
		httpMethod: 'POST',
		data: params,
		successCallback: successCallback,
		failCallback: function () {
			alert('파일 다운로드에 실패하였습니다.');
		}
	}).done(function () {});
};

var open_upload_excel_info_window = function (inputFileObj, header_info, upload_title, upload_url, extra_param) {
	if (upload_title == null) {
		upload_title = '엑셀 업로드';
	}
	
	window.upload_excel_info_window = {
		inputFileObj: inputFileObj,
		header_info: header_info,
		upload_title: upload_title,
		upload_url: upload_url,
		upload_extra_param: extra_param
	}
	
	var target    = 'upload_excel_info_window';
	var url       = '/popup/upload_excel_info_window';
	var post_data = null;
    var width     = 1346;
    var height    = 810;
    
    open_window(target, url, post_data, width, height);
}

// 쇼핑몰계정등록(자동) 팝업
var open_mall_user_info_window = function (mall_id) {
    var target    = 'basic_mall_user_info_window';
    var url       = '/basic/basic_mall/mall_user_info_window';
    var post_data = { mall_id: mall_id };
    var width     = 1346;
    var height    = 610;

    open_window(target, url, post_data, width, height);
};

// 쇼핑몰계정등록(수동) 팝업
var open_mall_info_window = function (mall_id) {
    var target    = 'basic_mall_info_window';
    var url       = '/basic/basic_mall/mall_info_window';
    var post_data = { mall_id: mall_id };
    var width     = 1346;
    var height    = 610;

    open_window(target, url, post_data, width, height);
};

// 매입처관리 팝업
var open_supplier_info_window = function (supplier_id) {
    var target    = 'basic_supplier_info_window';
    var url       = '/basic/basic_supplier/supplier_info_window';
    var post_data = { supplier_id: supplier_id ? supplier_id : '' };
    var width     = 1346;
    var height    = 610;

    open_window(target, url, post_data, width, height);
};

// 물류처관리 팝업
var open_logistic_info_window = function (logistic_id) {
    var target    = 'basic_logistic_info_window';
    var url       = '/basic/basic_logistic/logistic_info_window';
    var post_data = { logistic_id: logistic_id ? logistic_id : '' };
    var width     = 1346;
    var height    = 610;

    open_window(target, url, post_data, width, height);
};

// 카테고리 불러오기 팝업
var open_category_load_window = function () {
    var target = 'basic_category_load_window';
    var url = '/basic/basic_category/category_load_window';
    var width = 1000;
    var height = 500;

    open_window(target, url, null, width, height);
};

// 관리카테고리 등록 팝업
/**
 * type = [new,edit]
 * post_data key info
 * if( type = new)
 * code_l = 해당 코드가 null 이면 대분류 코드 등록
 * code_m = code_l = null and 해당 코드가 null 이면 중분류 코드 등록
 * code_s = code_l = null and code_m = null and 해당 코드가 null 이면 소분류 코드 등록
 * code_d = code_l = null and code_m = null and code_s = null and 해당 코드가 null 이면 세분류 코드 등록
 *
 *
 * if( type = edit)
 * code_l = 해당 코드는 필수! code_m,code_s,code_d 가 null이면 대분류 에대한 조회
 * code_m = code_l,code_m!=null 이고 code_s,code_d 가 null이면 중분류 에대한 조회
 * code_s = code_l,code_m,code_s!=null 이고 code_d 가 null이면 소분류 에대한 조회
 * code_d = code_l,code_m,code_s,code_d!=null 이면 세분류 에대한 조회
 *
 */
var open_category_info_window = function (type, post_data) {
    var target = 'basic_category_info_window';
    var url = '/basic/basic_category/category_info_window/' + type;
    var width = 800;
    var height = 450;

    open_window(target, url, post_data, width, height);
};

// 카테고리 매칭보기 팝업
var open_category_matching_window = function (post_data) {
    var target = 'category_matching_window';
    var url = '/basic/basic_category/category_matching_window';
    var width = 1280;
    var height = 800;

    open_window(target, url, post_data, width, height);
};

// 사용자정보 팝업
var open_customer_user_info_window = function (customer_user_id) {
    var url = '/basic/basic_user_perm/customer_user_info_window';
    var target = 'customer_user_info_window';
    var post_data = { customer_user_id: customer_user_id ? customer_user_id : '' };
    var width = 1024;
    var height = 450;

    open_window(target, url, post_data, width, height);
};

// 메뉴권한설정 팝업
var open_menu_permission_info_window = function (customer_user_id) {
    var url = '/basic/basic_user_perm/menu_permission_info_window';
    var target = 'menu_permission_info_window';
    var post_data = { customer_user_id: customer_user_id };
    var width = 1280;
    var height = 600;

    open_window(target, url, post_data, width, height);
};

// 기능권한설정 팝업
var open_menu_func_permission_info_window = function (customer_user_id) {
    var url = '/basic/basic_user_perm/menu_func_permission_info_window';
    var target = 'menu_func_permission_info_window';
    var post_data = { customer_user_id: customer_user_id };
    var width = 1280;
    var height = 600;

    open_window(target, url, post_data, width, height);
};

// 즐겨찾기설정 팝업
var open_quick_menu_info_window = function (quick_menu_id) {
    var url = '/basic/basic_quick_menu/quick_menu_info_window';
    var target = 'quick_menu_info_window';
    var post_data = { quick_menu_id: quick_menu_id ? quick_menu_id : '' };
    var width = 800;
    var height = 450;

    open_window(target, url, post_data, width, height);
};

// 상품정보수정 팝업
var open_product_modify_window = function(product_id) {
    if (product_id == 0) return;

    var target = 'product_modify_window';
    var url = '/popup/product_modify_window';
    var post_data = {
        product_id: product_id,
        type: 'EDIT'
    };
    var width = 1500;
    var height = 800;

    open_window(target, url, post_data, width, height);
};

// 상품 미리보기 팝업
var open_product_preview_window = function (product_id) {
    var target = 'product_preview_window';
    var url = '/popup/product_preview_window';
    var post_data = {
        product_id: product_id
    };
    var width = 1200;
    var height = 700;

    open_window(target, url, post_data, width, height);
};

// 쇼핑몰별 상품명 등록 팝업
var open_product_name_window = function (product_id) {
    var target = 'product_name_window';
    var url = '/popup/product_name_window';
    var post_data = {
        product_id: product_id
    };
    var width = 800;
    var height = 700;

    open_window(target, url, post_data, width, height);
};

// 쇼핑몰별 검색어 등록 팝업
var open_product_keyword_window = function (product_id) {
    var target = 'product_keyword_window';
    var url = '/popup/product_keyword_window';
    var post_data = {
        product_id: product_id
    };
    var width = 800;
    var height = 700;

    open_window(target, url, post_data, width, height);
};

// 쇼핑몰별 정보고시 등록 팝업
var open_product_notify_window = function (product_id) {
    var target = 'product_notify_window';
    var url = '/popup/product_notify_window';
    var post_data = {
        product_id: product_id
    };
    var width = 800;
    var height = 700;

    open_window(target, url, post_data, width, height);
};

// 쇼핑몰별 판매가 등록 팝업
var open_product_price_window = function (product_id) {
    var target = 'product_price_window';
    var url = '/popup/product_price_window';
    var post_data = {
        product_id: product_id
    };
    var width = 800;
    var height = 700;

    open_window(target, url, post_data, width, height);
};

// 상세설명 미리보기 팝업
var open_detail_note_preview_window = function (head_note, detail_note, tail_note) {
    var target = 'detail_note_preview_window';
    var url = '/popup/detail_note_preview_window';
    var post_data = {
        head_note: head_note,
        detail_note: detail_note,
        tail_note: tail_note
    };

    var width = 1200;
    var height = 700;

    open_window(target, url, post_data, width, height);
};

// 쇼핑몰별 상세설명 등록 팝업
var open_product_detail_note_window = function (product_id) {
    var target = 'product_detail_note_window';
    var url = '/popup/product_detail_note_window';
    var post_data = {
        product_id: product_id
    };
    var width = 800;
    var height = 700;

    open_window(target, url, post_data, width, height);
};

// 쇼핑몰별 재고분할등록
var open_product_weight_window = function (product_id) {
    var target = 'product_weight_window';
    var url = '/popup/product_weight_window';
    var post_data = {
        product_id: product_id
    };
    var width = 800;
    var height = 700;

    open_window(target, url, post_data, width, height);
};

// 옵션별 세트구성등록
var open_product_stock_set_info_window = function (product_id, option_code) {
    var target = 'product_stock_set_info_window';
    var url = '/popup/product_stock_set_info_window';
    var post_data = {
        product_id: product_id,
        option_code: option_code
    };
    var width = 800;
    var height = 700;

    open_window(target, url, post_data, width, height);
};

var open_product_log_info_window = function (product_id) {
    var target = 'product_log_info_window';
    var url = '/product/product_list/product_log_info_window';
    var post_data = {
        product_id: product_id
    };
    var width = 1000;
    var height = 550;

    open_window(target, url, post_data, width, height);
};

var open_product_send_info_window = function (product_id) {
    var target = 'product_send_info_window';
    var url = '/product/product_list/product_send_info_window';
    var post_data = {
        product_id: product_id
    };
    var width = 1000;
    var height = 550;

    open_window(target, url, post_data, width, height);
}

// 추가상품정보 - 추가상품그룹 선택 팝업
var open_add_good_search_info_window = function (product_id) {
    var target = 'add_good_search_info_window';
    var url = '/product/product_insert_form/add_good_search_info_window';
    var post_data = {
        product_id: product_id
    };
    var width = 1150;
    var height = 700;

    open_window(target, url, post_data, width, height);
};

// 상품정보복사 팝업
var open_product_copy_window = function(product_id) {
    var target = 'product_modify_window';
    var url = '/popup/product_modify_window';
    var post_data = {
        product_id: product_id,
        type: 'COPY'
    };
    var width = 1500;
    var height = 800;

    open_window(target, url, post_data, width, height);
};

// 상품옵션수정 팝업
var open_product_option_modify_window = function (product_id) {
    var target = 'product_option_modify_window';
    var url = '/popup/product_option_modify_window';
    var post_data = { product_id: product_id };
    var width = 1500;
    var height = 800;

    open_window(target, url, post_data, width, height);
};

// 정보고시수정 팝업
var open_product_notify_modify_window = function (product_id) {
    var target = 'product_notify_modify_window';
    var url = '/popup/product_notify_modify_window';
    var post_data = { product_id: product_id };
    var width = 1500;
    var height = 800;

    open_window(target, url, post_data, width, height);
};

var open_etc_solution_linkage_option_change_temporary_window = function (etc_solution, data) {
    window.etc_solution_linkage_option_change_temporary_window = {
        data: data
    };
    var target = 'etc_solution_linkage_option_change_temporary_window';
    var url = '/popup/product/product_option/etc_solution_linkage_option_change_temporary_window';
    var post_data = {
        etc_solution: etc_solution
    };
    var width = 1280;
    var height = 768;

    open_window(target, url, post_data, width, height);
};

// 연동정보 팝업
var open_linkage_info_window = function (type, mall_id, linkage_id) {
    if (!mall_id) {
        alert('쇼핑몰을 선택해주세요.');
        return;
    }

    if (mall_id == 1009 || mall_id == 1011 || mall_id == 1063) {
        alert("해당몰은 지원하지 않습니다.");
        return;
    }

    var target = 'linkage_info_window';
    var url = '/popup/linkage_info_window/' + mall_id + '/linkage_info';
    var post_data = {
    	type: type,
        linkage_id: linkage_id ? linkage_id : ''
    };
    var width = 1200;
    var height = 800;

    open_window(target, url, post_data, width, height);
};

// 카테고리정보 팝업
var open_linkage_category_info_window = function (type, mall_id, linkage_category_id, customer_category_id) {
    if (!mall_id) {
        alert('쇼핑몰을 선택해주세요.');
        return;
    }

    if (mall_id == 10010 || mall_id == 10060) {
        alert("해당몰은 지원하지 않습니다.");
        return;
    }

    var target = 'linkage_category_info_window';
    var url = '/popup/linkage_info_window/' + mall_id + '/linkage_category_info';
    var post_data = {
    	type: type,
        mall_id: mall_id,
        linkage_category_id: linkage_category_id ? linkage_category_id : '',
        customer_category_id: customer_category_id ? customer_category_id : ''
    };
    var width = 1200;
    var height = 800;

    open_window(target, url, post_data, width, height);
};

// 배송정보 팝업
var open_linkage_delivery_info_window = function (type, mall_id, linkage_delivery_id) {
    if (!mall_id) {
        alert('쇼핑몰을 선택해주세요.');
        return;
    }

    if (mall_id == 10010 || mall_id == 10021 || mall_id == 10031 || mall_id == 10032 || mall_id == 10060 || mall_id == 10074 || mall_id == 10082 || mall_id == 10087 || mall_id == 10089 || mall_id == 10090 || mall_id == 10091 || mall_id == 10095) {
        alert("해당몰은 지원하지 않습니다.");
        return;
    }

    var target = 'linkage_delivery_info_window';
    var url = '/popup/linkage_info_window/' + mall_id + '/linkage_delivery_info';
    var post_data = {
    	type: type,
        mall_id: mall_id,
        linkage_delivery_id: linkage_delivery_id ? linkage_delivery_id : ''
    };
    var width = 1200;
    var height = 800;

    open_window(target, url, post_data, width, height);
};

var open_linkage_minimum_price_info_window = function () {
    var target = 'linkage_minimum_price_info_window';
    var url = '/popup/linkage/linkage_modify/linkage_minimum_price_info_window';
    var post_data = null;
    var width = 1280;
    var height = 768;

    open_window(target, url, post_data, width, height);
};

// 쇼핑몰카테고리 일괄수정 팝업
var open_linkage_category_temporary_window = function (data) {
    window.linkage_category_temporary_window = {
        data: data
    };
    var target = 'linkage_category_temporary_window';
    var url = '/popup/linkage/linkage_category/linkage_category_temporary_window';
    var post_data = null;
    var width = 1280;
    var height = 768;

    open_window(target, url, post_data, width, height);
};

var open_etc_solution_product_temporary_window = function (etc_solution, data) {
    window.etc_solution_product_temporary_window = {
        data: data
    };
    var target = 'etc_solution_product_temporary_window';
    var url = '/popup/linkage/linkage_modify/etc_solution_product_temporary_window';
    var post_data = {
        etc_solution: etc_solution
    };
    var width = 1280;
    var height = 768;

    open_window(target, url, post_data, width, height);
};

var open_etc_solution_linkage_info_temporary_window = function (etc_solution, data) {
    window.etc_solution_linkage_info_temporary_window = {
        data: data
    };
    var target = 'etc_solution_linkage_info_temporary_window';
    var url = '/popup/linkage/linkage_info/etc_solution_linkage_info_temporary_window';
    var post_data = {
        etc_solution: etc_solution
    };
    var width = 1280;
    var height = 768;

    open_window(target, url, post_data, width, height);
};

var open_etc_solution_linkage_category_info_temporary_window = function (etc_solution, data) {
    window.etc_solution_linkage_category_info_temporary_window = {
        data: data
    };
    var target = 'etc_solution_linkage_category_info_temporary_window';
    var url = '/popup/linkage/linkage_category/etc_solution_linkage_category_info_temporary_window';
    var post_data = {
        etc_solution: etc_solution
    };
    var width = 1280;
    var height = 768;

    open_window(target, url, post_data, width, height);
};

var open_linkage_product_info_window = function (linkage_product_id, order_id) {
    var target = 'linkage_product_info_window';
    var url = '/product/product_list/linkage_product_info_window';
    var post_data = {
    	linkage_product_id: linkage_product_id ? linkage_product_id : '',
    	order_id: order_id ? order_id : ''
    };
    var width = 950;
    var height = 570;

    open_window(target, url, post_data, width, height);
};

var open_linkage_search_info_window = function (mall_id, mall_user_id, callback) {
    window.linkage_search_info_window = {
        callback: callback
    };

    var target = 'linkage_search_info_window';
    var url = '/popup/linkage_search_info_window';
    var post_data = {
        mall_id: mall_id,
        mall_user_id: mall_user_id
    };
    var width = 1200;
    var height = 800;

    open_window(target, url, post_data, width, height);
};

var open_linkage_category_search_info_window = function (mall_id, mall_user_id, callback) {
    window.linkage_category_search_info_window = {
        callback: callback
    };

    var target = 'linkage_category_search_info_window';
    var url = '/popup/linkage_category_search_info_window';
    var post_data = {
        mall_id: mall_id,
        mall_user_id: mall_user_id
    };
    var width = 1200;
    var height = 800;

    open_window(target, url, post_data, width, height);
};

var open_linkage_delivery_search_info_window = function (mall_id, mall_user_id, callback) {
    window.linkage_delivery_info_search_window = {
        callback: callback
    };

    var target = 'linkage_delivery_search_info_window';
    var url = '/popup/linkage_delivery_search_info_window';
    var post_data = {
        mall_id: mall_id,
        mall_user_id: mall_user_id
    };
    var width = 1200;
    var height = 800;

    open_window(target, url, post_data, width, height);
};

var open_linkage_product_send_info_window = function (product_id_string, product_name_string, sale_price_string) {
	var target = 'linkage_product_send_info_window';
    var url = '/product/product_insert_form/linkage_product_send_info_window';
    var post_data = {
    	product_id_string: product_id_string,
    	product_name_string: product_name_string,
    	sale_price_string: sale_price_string
    };
    var width = 1280;
    var height = 768;
    
    open_window(target, url, post_data, width, height);
};

var open_product_modify_info_window = function () {
	var target = 'product_xls_modify_info_window';
    var url = '/product/product_xls_modify/product_xls_modify_info_window';
    var post_data = null;
    var width = 1280;
    var height = 768;

    open_window(target, url, post_data, width, height);
};

var open_linkage_product_modify_info_window = function (send_data_string) {
	var target = 'linkage_product_modify_info_window';
    var url = '/product/product_list/linkage_product_modify_info_window';
    var post_data = {
    		send_data_string: send_data_string
        };
    var width = 400;
    var height = 700;

    open_window(target, url, post_data, width, height);
}

// 주문상세 팝업
var open_order_info_window = function (order_id, location_id) {
    var location_id = location_id ? '#' + location_id : ''
    var target = 'order_info_window';
    var url = '/popup/order_info_window' + location_id;
    var post_data = {
        order_id: order_id
    };
    var width = 1350;
    var height = 768;

    open_window(target, url, post_data, width, height);
};

// C/S답변등록 팝업
var open_order_customer_service_answer_info_window = function (order_customer_service_id) {
    var target = 'order_customer_service_answer_info_window';
    var url = '/popup/order_customer_service_answer_info_window';
    var post_data = {
        order_customer_service_id: order_customer_service_id
    };
    var width = 500;
    var height = 250;

    open_window(target, url, post_data, width, height);
};

// 주문복사 팝업
var open_order_copy_info_window = function (order_id) {
    var target = 'order_copy_info_window';
    var url = '/popup/order_copy_info_window';
    var post_data = {
        order_id: order_id
    };
    var width = 1280;
    var height = 768;

    open_window(target, url, post_data, width, height);
};

// 주문분리 팝업
var open_order_divide_info_window = function (order_id) {
    var target = 'order_divide_info_window';
    var url = '/popup/order_divide_info_window';
    var post_data = {
        order_id: order_id
    };
    var width = 1280;
    var height = 768;

    open_window(target, url, post_data, width, height);
};

// 주문상태도 팝업
var open_order_status_info_window = function () {
	var target = 'order_status_info_window';
    var url = '/order/order_matching/order_status_info_window';
    var post_data = null;
    var width = 950;
    var height = 520;

    open_window(target, url, post_data, width, height);
}

// 주문엑셀폼 팝업
var open_order_download_excel_form_list_info_window = function () {
    var target = 'order_download_excel_form_list_info_window';
    var url = '/popup/order_download_excel_form_list_info_window';
    var post_data = null;
    var width = 1280;
    var height = 768;

    open_window(target, url, post_data, width, height);
};

// 주문엑셀폼 추가 팝업
var open_order_download_excel_form_info_window = function (order_download_excel_form_id) {
    var target = 'order_download_excel_form_info_window';
    var url = '/popup/order_download_excel_form_info_window';
    var post_data = {
        order_download_excel_form_id: order_download_excel_form_id
    };
    var width = 1280;
    var height = 768;

    open_window(target, url, post_data, width, height);
};

// 엑셀 간략받기 팝업
var open_order_simplic_info_window = function () {
    var target = 'order_simplic_info_window';
    var url = '/popup/order_simplic_info_window';
    var post_data = null;
    var width = 1280;
    var height = 768;

    open_window(target, url, post_data, width, height);
};

// SMS 팝업
var open_sms_info_window = function (send_data) {
    var target = 'sms_info_window';
    var url = '/popup/sms_info_window';
    var post_data = send_data;
    var width = 1000;
    var height = 590;

    open_window(target, url, post_data, width, height);
};

// 주문상태변경 팝업
var open_change_order_status_info_window = function (order_ids, change_order_status) {
    var target = 'change_order_status_info_window';
    var url = '/order/order_matching/change_order_status_info_window';
    var post_data = {
        order_ids: order_ids,
        change_order_status: change_order_status
    };
    var width = 750;
    var height = 500;

    open_window(target, url, post_data, width, height);
};

var open_gift_list_info_window = function () {
    var target    = 'gift_list_info_window';
    var url       = '/order/order_matching/gift_list_info_window';
    var post_data = null;
    var width     = 1000;
    var height    = 500;

    open_window(target, url, post_data, width, height);
};

var open_gift_info_window = function (gift_id) {
    var target    = 'order_gift_info_window';
    var url       = '/order/order_matching/gift_info_window';
    var post_data = {
        gift_id: gift_id ? gift_id : ''
    };
    var width     = 1000;
    var height    = 500;

    open_window(target, url, post_data, width, height);
};

var open_order_barcode_scan_info_window = function () {
    var target = 'order_barcode_scan_info_window';
    var url = '/order/order_invoice/order_barcode_scan_info_window';
    var post_data = null;
    var width = 1000;
    var height = 750;

    open_window(target, url, post_data, width, height);
};

var open_order_packing_scan_info_window = function () {
    var target = 'order_packing_scan_info_window';
    var url = '/order/order_invoice/order_packing_scan_info_window';
    var post_data = null;
    var width = 1200;
    var height = 750;

    open_window(target, url, post_data, width, height);
};

var open_upload_window = function (target, title, upload_url, upload_data, total_count, callback) {
    window.upload_window = {
        data: upload_data,
        callback: callback
    };

    var url = '/popup/upload_window';
    var post_data = {
        process_title: title,
        process_url: upload_url,
        total_count: total_count
    };
    var width = 500;
    var height = 250;

    open_window(target, url, post_data, width, height);
};

var open_download_window = function (download_type, filename, target, title, download_url, download_total_count, download_params, etc_param1) {
    var url = '/popup/download_window';
    var post_data = {
        download_type: download_type,
        filename: filename,
        process_title: title,
        process_url: download_url,
        process_data: download_params,
        total_count: download_total_count,
        etc_param1: etc_param1 ? etc_param1 : ''
    };
    var width = 1280;
    var height = 768;

    open_window(target, url, post_data, width, height);
};

// 정보고시 업로드 결과 팝업
var open_product_notify_temporary_window = function (data, assort_code, assort_name) {
    window.product_notify_temporary_window = {
        data: data,
        assort_code: assort_code,
        assort_name: assort_name
    };
    var target = 'product_notify_temporary_window';
    var url = '/popup/product_notify_temporary_window';
    var post_data = null;
    var width = 1280;
    var height = 768;

    open_window(target, url, post_data, width, height);
};

// 쇼핑몰 판매가 업로드 결과 팝업
var open_linkage_price_temporary_window = function (data) {
    window.linkage_price_temporary_window = {
        data: data
    };
    var target = 'linkage_price_temporary_window';
    var url = '/popup/linkage_price_temporary_window';
    var post_data = null;
    var width = 1280;
    var height = 768;

    open_window(target, url, post_data, width, height);
};

// 쇼핑몰 상품명 업로드 결과 팝업
var open_linkage_product_name_temporary_window = function (data) {
    window.linkage_product_name_temporary_window = {
        data: data
    };
    var target = 'linkage_product_name_temporary_window';
    var url = '/popup/linkage_product_name_temporary_window';
    var post_data = null;
    var width = 1280;
    var height = 768;

    open_window(target, url, post_data, width, height);
};

// 쇼핑몰별 상세설명 업로드 결과 팝업
var open_linkage_detail_note_temporary_window = function (data) {
    window.linkage_detail_note_temporary_window = {
        data: data
    };
    var target = 'linkage_detail_note_temporary_window';
    var url = '/popup/linkage_detail_note_temporary_window';
    var post_data = null;
    var width = 1280;
    var height = 768;

    open_window(target, url, post_data, width, height);
};

// 카테고리 업로드 결과 팝업
var open_category_temporary_window = function (data) {
    window.basic_category_temporary_window = {
        data: data
    };
    var target = 'basic_category_temporary_window';
    var url = '/popup/basic/basic_category/basic_category_temporary_window';
    var post_data = null;
    var width = 1280;
    var height = 768;

    open_window(target, url, post_data, width, height);
};

// 상품 업로드 결과 팝업
var open_product_xls_temporary_window = function (form, mall_user_id, data) {
    window.product_xls_temporary_window = {
        data: data
    };
    var target = 'product_xls_temporary_window';
    var url = '/popup/product_xls_temporary_window';
    var post_data = {
        form: form,
        mall_user_id: mall_user_id
    };
    var width = 1280;
    var height = 768;

    open_window(target, url, post_data, width, height);
};

// 쇼핑몰 판매가 업로드 결과 팝업
var open_product_stock_temporary_window = function (data) {
    window.product_stock_temporary_window = {
        data: data
    };
    var target = 'product_stock_temporary_window';
    var url = '/popup/product_stock_temporary_window';
    var post_data = null;
    var width = 1280;
    var height = 768;

    open_window(target, url, post_data, width, height);
};

// 옵션세트구성관리 엑셀업로드
var open_product_stock_set_temporary_window = function (data) {
    window.product_stock_temporary_window = {
        data: data
    };
    var target = 'product_stock_set_temporary_window';
    var url = '/popup/product_stock_set_temporary_window';
    var post_data = null;
    var width = 1280;
    var height = 768;

    open_window(target, url, post_data, width, height);
};

// 택배사코드 보기 팝업
var open_delivery_info_window = function () {
    var target = 'delivery_info_window';
    var url = '/popup/delivery_info_window';
    var post_data = null;
    var width = 800;
    var height = 700;

    open_window(target, url, post_data, width, height);
};

// 송장번호 엑셀등록 팝업
var open_invoice_upload_info_window = function () {
    var target = 'invoice_upload_info_window';
    var url = '/order/order_invoice/invoice_upload_info_window';
    var post_data = null;
    var width = 1000;
    var height = 600;

    open_window(target, url, post_data, width, height);
};

// 대량등록양식 만들기 팝업
var open_order_invoice_form_window = function () {
    var target = 'order_invoice_form_window';
    var url = '/order/order_invoice/order_invoice_form_window';
    var post_data = null;
    var width = 1000;
    var height = 600;

    open_window(target, url, post_data, width, height);
};

// 택배사별 송장대량등록 양식 신규등록 팝업
var open_order_invoice_form_add_window = function (invoice_upload_excel_form_id) {
    var target = 'order_invoice_form_add_window';
    var url = '/order/order_invoice/order_invoice_form_add_window';
    var post_data = {
    	invoice_upload_excel_form_id: invoice_upload_excel_form_id ? invoice_upload_excel_form_id : ''
    };
    var width = 1000;
    var height = 600;

    open_window(target, url, post_data, width, height);
};

// 카테고리 보기 팝업
var open_category_window = function (type) {
    var target = 'category_window';
    var url = '/basic/basic_category/category_window/' + type;
    var post_data = null;
    var width = 1200;
    var height = 600;

    open_window(target, url, post_data, width, height);
};

// 주문수집 팝업
var open_order_collecting_window = function (post_data) {
    var target = 'order_collecting_window';
    var url = '/popup/order_collecting_window';
    var width = 1200;
    var height = 600;

    open_window(target, url, post_data, width, height);
};

var open_product_auto_temporary_window = function () {
    var url = "/popup/product_auto_temporary_window";
    var target = "product_auto_temporary_window";
    var post_data = null;
    var width = 1250;
    var height = 600;

    open_window(target, url, post_data, width, height);
};

// 추가상품관리 신규추가옵션셋팅 팝업
var open_add_good_info_window = function(add_good_id) {
    var target = 'add_good_info_window';
    var url = '/product/product_xls_modify/add_good_info_window';
    var post_data = {
        add_good_id: add_good_id ? add_good_id : ''
    };
    var width = 1000;
    var height = 600;

    open_window(target, url, post_data, width, height);
};

var open_linkage_option_change_info_window = function (linkage_option_change_id) {
    var target = 'linkage_option_change_info_window';
    var url = '/popup/product/product_option/linkage_option_change_info_window';
    var post_data = {
        linkage_option_change_id: linkage_option_change_id ? linkage_option_change_id : ''
    };
    var width = 650;
    var height = 400;

    open_window(target, url, post_data, width, height);
};

var open_collect_order_window = function (mall_user_ids, start_at, end_at, order_checked, setting_data) {
	var target = 'linkage_option_change_info_window';
    var url = '/popup/common/collect_order_window';
    var post_data = {
    		mall_user_ids: mall_user_ids,
    		start_at: start_at,
    		end_at: end_at,
    		order_checked: order_checked,
    		setting_data: setting_data
    };
    var width = 650;
    var height = 400;

    open_window(target, url, post_data, width, height);
}

var open_product_option_info_window = function (target, params, callback) {
    window.product_option_info_window = {
        target: target,
        callback: callback
    };

    target = 'product_option_info_window';
    var url = '/popup/product_option_info_window';
    var post_data = params;
    var width = 1300;
    var height = 600;

    open_window(target, url, post_data, width, height);
};

var open_order_matching_info_window = function (order_id) {
    var target = 'order_matching_info_window';
    var url = '/order/order_matching/order_matching_info_window';
    var post_data = {
        order_id: order_id
    };
    var width = 1400;
    var height = 750;

    open_window(target, url, post_data, width, height);
};

var open_excel_form_add_window = function (mall_id) {
    var target = 'order_excel_form_add_window';
    var url = '/popup/order/order_excel/order_excel_form_add_window';
    var post_data = {
        mall_id: mall_id
    };
    var width = 1024;
    var height = 768;

    open_window(target, url, post_data, width, height);
};

var open_invoice_band_info_window = function (delivery) {
	var target = 'invoice_band_info_window';
    var url = '/order/order_invoice/invoice_band_info_window';
    var post_data = {
    	delivery: delivery
    };
    var width = 600;
    var height = 350;

    open_window(target, url, post_data, width, height);
}

var open_inquiry_info_window = function (inquiry_id) {
    var target = 'inquiry_info_window';
    var url = '/order/order_claim_cs/inquiry_info_window';
    var post_data = {
        inquiry_id: inquiry_id
    };
    var width = 1000;
    var height = 500;

    open_window(target, url, post_data, width, height);
};

var open_inquiry_answer_list_info_window = function (answer_type) {
    var target = 'inquiry_answer_list_info_window';
    var url = '/order/order_claim_cs/inquiry_answer_list_info_window';
    var post_data = {
    	answer_type: answer_type
    }
    var width = 900;
    var height = 500;

    open_window(target, url, post_data, width, height);
};

var open_service_mall_notice_info_window = function (mall_notice_id) {
    var target = 'service_mall_notice_info_window';
    var url = '/popup/service/service_mall_notify/service_mall_notice_info_window';
    var post_data = {
        mall_notice_id: mall_notice_id ? mall_notice_id : ''
    };
    var width = 1000;
    var height = 820;

    open_window(target, url, post_data, width, height);
};

var open_service_notice_write_window = function (category, notice_id) {
    var target = 'service_notice_write_window';
    var url = '/popup/service/service_notify/service_notice_write_window';
    var post_data = {
        category: category,
        notice_id: notice_id ? notice_id : ''
    };
    var width = 850;
    var height = 680;

    open_window(target, url, post_data, width, height);
};

var open_service_notice_info_window = function (notice_id) {
    var target = 'service_notice_info_window';
    var url = '/popup/service/service_notify/service_notice_info_window';
    var post_data = {
        notice_id: notice_id
    };
    var width = 850;
    var height = 680;

    open_window(target, url, post_data, width, height);
};

// 온라인 문의 답변등록 팝업
var open_service_notice_answer_window = function (notice_id, notice_answer_id) {
    var target = 'service_notice_answer_window';
    var url = '/popup/service/service_notify/service_notice_answer_window';
    var post_data = {
        notice_id: notice_id,
        notice_answer_id: notice_answer_id ? notice_answer_id : ''
    };
    var width = 850;
    var height = 900;

    open_window(target, url, post_data, width, height);
};

var open_service_notice_answer_info_window = function (notice_answer_id) {
    var target = 'service_notice_answer_info_window';
    var url = '/popup/service/service_notify/service_notice_answer_info_window';
    var post_data = {
        notice_answer_id: notice_answer_id
    };
    var width = 850;
    var height = 650;

    open_window(target, url, post_data, width, height);
};

// 약정서비스 내용 팝업
var open_service_contract_content_window = function (customer_contract_id) {
    var target    = 'service_contract_content_window';
    var url       = '/popup/service/service_contract/service_contract_content_window';
    var post_data = {
        customer_contract_id: customer_contract_id
    };
    var width = 850;
    var height = 555;

    open_window(target, url, post_data, width, height);
};

// 계약서 복사 팝업
var open_service_contract_copy_window = function (customer_contract_id) {
    var target    = 'service_contract_copy_window';
    var url       = '/popup/service/service_contract/service_contract_copy_window';
    var post_data = {
        customer_contract_id: customer_contract_id
    };
    var width = 900;
    var height = 850;

    open_window(target, url, post_data, width, height);
};

// 계약서 팝업
var open_service_contract_info_window = function (customer_contract_id) {
    var target    = 'service_contract_info_window';
    var url       = '/popup/service/service_contract/service_contract_info_window';
    var post_data = {
        customer_contract_id: customer_contract_id
    };
    var width = 900;
    var height = 940;

    open_window(target, url, post_data, width, height);
};

// 개인정보위수탁 팝업
var open_service_contract_privacy_window = function (customer_contract_id) {
    var target    = 'service_contract_privacy_window';
    var url       = '/popup/service/service_contract/service_contract_privacy_window';
    var post_data = {
        customer_contract_id: customer_contract_id
    };
    var width = 850;
    var height = 940;

    open_window(target, url, post_data, width, height);
};

var open_customer_info_window = function (customer_id) {
    var target = 'admin_customer_list_info_window';
    var url = '/admin/customer_list/customer_info_window';
    var post_data = {
        customer_id: customer_id
    };
    var width     = 1346;
    var height    = 610;

    open_window(target, url, post_data, width, height);
};

var open_customer_memo_info_window = function (customer_id) {
    var target = 'customer_memo_info_window';
    var url = '/admin/customer_list/customer_memo_info_window';
    var post_data = {
        customer_id: customer_id
    };
    var width     = 1346;
    var height    = 610;

    open_window(target, url, post_data, width, height);
};

var open_customer_contract_info_window = function (type, customer_contract_id) {
    var target    = 'customer_contract_info_window';
    var url       = '/popup/admin/admin_payment/customer_contract_info_window/' + type;
    var post_data = {
        customer_contract_id: customer_contract_id
    };
    var width     = 1100;
    var height    = 823;

    open_window(target, url, post_data, width, height);
};

// 빠른결제 검색 팝업
var open_admin_card_search_window = function (callback) {
    window.admin_card_search_window =
    {
        callback: callback
    };
    var target = 'admin_card_search_window';
    var url = '/popup/admin/admin_payment/admin_card_search_window';
    var post_data = null;
    var width = 900;
    var height = 700;

    open_window(target, url, post_data, width, height);
};

var open_adjustment_generate_info_window = function (supplier_id, supplier_name, start_at, end_at, delivery_count, delivery_amount, cancel_count, cancel_amount, return_count, return_amount, sale_amount, buyer_amount, diff_amount) {
    var target    = 'adjustment_generate_info_window';
    var url       = '/popup/adjustment/adjustment_generate/adjustment_generate_info_window';
    var post_data = {
        supplier_id: supplier_id,
        supplier_name: supplier_name,
        start_at: start_at,
        end_at: end_at,
        delivery_count: delivery_count,
        delivery_amount: delivery_amount,
        cancel_count: cancel_count,
        cancel_amount: cancel_amount,
        return_count: return_count,
        return_amount: return_amount,
        sale_amount: sale_amount,
        buyer_amount: buyer_amount,
        diff_amount: diff_amount
    };
    var width     = 1100;
    var height    = 823;

    open_window(target, url, post_data, width, height);
};

var open_adjustment_modify_order_info_window = function (adjustment_id) {
    var target = 'adjustment_modify_order_info_window';
    var url = '/popup/adjustment/adjustment_modify/adjustment_modify_order_info_window';
    var post_data = {
        adjustment_id: adjustment_id
    };
    var width = 1024;
    var height = 768;

    open_window(target, url, post_data, width, height);
};

var open_adjustment_modify_add_info_window = function (adjustment_id) {
    var target = 'adjustment_modify_add_info_window';
    var url = '/popup/adjustment/adjustment_modify/adjustment_modify_add_info_window';
    var post_data = {
        adjustment_id: adjustment_id
    };
    var width = 1024;
    var height = 768;

    open_window(target, url, post_data, width, height);
};

var open_adjustment_end_deposit_info_window = function (adjustment_id) {
    var target = 'adjustment_end_deposit_info_window';
    var url = '/popup/adjustment/adjustment_end/adjustment_end_deposit_info_window';
    var post_data = {
        adjustment_id: adjustment_id
    };
    var width = 600;
    var height = 400;

    open_window(target, url, post_data, width, height);
};

var open_adjustment_end_tax_info_window = function (adjustment_id) {
    var target = 'adjustment_end_tax_info_window';
    var url = '/popup/adjustment/adjustment_end/adjustment_end_tax_info_window';
    var post_data = {
        adjustment_id: adjustment_id
    };
    var width = 600;
    var height = 400;

    open_window(target, url, post_data, width, height);
};

var open_adjustment_flect_temporary_window = function (mall_id, data) {
    window.adjustment_flect_temporary_window = {
        data: data
    };

    var target = 'adjustment_flect_temporary_window';
    var url = '/popup/adjustment/adjustment_flect/adjustment_flect_temporary_window';
    var post_data = {
        mall_id: mall_id
    };
    var width = 1024;
    var height = 768;

    open_window(target, url, post_data, width, height);
};

// 매입처정산내역 업로드 결과 팝업
var open_adjustment_list_temporary_window = function (data) {
    window.adjustment_list_temporary_window = {
        data: data
    };
    var target = 'adjustment_list_temporary_window';
    var url = '/popup/adjustment/adjustment_list/adjustment_list_temporary_window';
    var post_data = null;
    var width = 1280;
    var height = 768;

    open_window(target, url, post_data, width, height);
};

var open_stock_product_info_window = function (stock_product_id) {
    var target = 'stock_product_info_window';
    var url = '/stock/stock_product/stock_product_info_window';
    var post_data = {
        stock_product_id: stock_product_id ? stock_product_id : ''
    };
    var width = 1000;
    var height = 400;

    open_window(target, url, post_data, width, height);
};

var open_warehouse_in_info_window = function (warehouse_in_id) {
    var target = 'warehouse_in_info_window';
    var url = '/stock/warehouse_in/warehouse_in_info_window';
    var post_data = {
        warehouse_in_id: warehouse_in_id ? warehouse_in_id : ''
    };
    var width = 1350;
    var height = 700;

    open_window(target, url, post_data, width, height);
};

var open_warehouse_out_info_window = function (warehouse_out_id) {
    var target = 'warehouse_out_info_window';
    var url = '/stock/warehouse_out/warehouse_out_info_window';
    var post_data = {
        warehouse_out_id: warehouse_out_id ? warehouse_out_id : ''
    };
    var width = 1350;
    var height = 700;

    open_window(target, url, post_data, width, height);
};

var open_warehouse_move_info_window = function (warehouse_move_id) {
    var target = 'warehouse_move_info_window';
    var url = '/stock/warehouse_move/warehouse_move_info_window';
    var post_data = {
        warehouse_move_id: warehouse_move_id ? warehouse_move_id : ''
    };
    var width = 1350;
    var height = 700;

    open_window(target, url, post_data, width, height);
};

var open_warehouse_actuality_info_window = function (warehouse_actuality_id) {
    var target = 'warehouse_actuality_info_window';
    var url = '/stock/warehouse_actuality/warehouse_actuality_info_window';
    var post_data = {
        warehouse_actuality_id: warehouse_actuality_id ? warehouse_actuality_id : ''
    };
    var width = 1350;
    var height = 700;

    open_window(target, url, post_data, width, height);
};

var open_warehouse_deadline_info_window = function (start_at, end_at) {
    var target = 'warehouse_deadline_info_window';
    var url = '/stock/warehouse_deadline/warehouse_deadline_info_window';
    var post_data = {
        start_at: start_at,
        end_at: end_at
    };
    var width = 600;
    var height = 400;

    open_window(target, url, post_data, width, height);
};

// 신용카드 전표출력 팝업
var open_card_receipt_info_window = function (id, receipt_type) {
    $.ajax({
    	type: 'POST',
        url: '/common/card_receipt_info',
        data: {
            customer_contract_id: id,
            receipt_type: receipt_type
        },
        success: function (data) {
            if (data.error_msg) {
                alert(data.error_msg);
            } else {
                var target = 'card_receipt_info_window';
                var url = data.url;
                var post_data = null;
                var width = 500;
                var height = 850;

                open_window(target, url, post_data, width, height);
            }
        }
    });
};

var open_order_allocation_info_window = function (params, callback) {
    window.order_allocation_info_window = {
        params: params,
        callback: callback
    };

    var target = 'order_allocation_info_window';
    var url = '/popup/order/order_allocation/order_allocation_info_window';
    var post_data = null;
    var width = 500;
    var height = 250;

    open_window(target, url, post_data, width, height);
};

var open_order_allocation_bundler_info_window = function () {
    var target = 'order_allocation_bundler_info_window';
    var url = '/popup/order/order_allocation/order_allocation_bundler_info_window';
    var post_data = null;
    var width = 800;
    var height = 400;

    open_window(target, url, post_data, width, height);
};

var open_order_allocation_send_info_window = function (callback) {
    var target = 'order_allocation_send_info_window';
    var url = '/popup/order/order_allocation/order_allocation_send_info_window';
    var post_data = null;
    var width = 800;
    var height = 400;

    open_window(target, url, post_data, width, height);
};

var open_order_send_receive_invoice_window = function (callback) {
    window.order_send_receive_invoice_window = {
        callback: callback
    };

    var target = 'order_send_receive_invoice_window';
    var url = '/popup/order/order_send/order_send_receive_invoice_window';
    var post_data = null;
    var width = 500;
    var height = 250;

    open_window(target, url, post_data, width, height);
};

var open_order_send_receive_use_stock_window = function (callback) {
    window.order_send_receive_use_stock_window = {
        callback: callback
    };

    var target = 'order_send_receive_use_stock_window';
    var url = '/popup/order/order_send/order_send_receive_use_stock_window';
    var post_data = null;
    var width = 500;
    var height = 250;

    open_window(target, url, post_data, width, height);
};

var open_domesin_regist_info_window = function () {
    var target = 'domesin_regist_info_window';
    var url = '/popup/product/product_auto/domesin_regist_info_window';
    var post_data = null;
    var width = 800;
    var height = 800;

    open_window(target, url, post_data, width, height);
};

var open_domesin_product_info_window = function () {
    var target = 'domesin_product_info_window';
    var url = '/popup/product/product_auto/domesin_product_info_window';
    var post_data = null;
    var width = 1000;
    var height = 800;

    open_window(target, url, post_data, width, height);
};

//결제관리창 문자 포인트 충전 팝업
var open_contract_send_window = function () {
    var target = 'contract_sms_send_window';
    var url = '/admin/contract_sms/contract_sms_insert_window';
    var post_data = null;
    var width = 1000;
    var height = 450;

    open_window(target, url, post_data, width, height);
};

//결제관리창 카드결제 팝업
var open_contract_card_send_window = function (data) {
    var target = 'contract_sms_card_send_window';
    var url = '/admin/contract_sms/contract_sms_insert_card_window';
    var post_data = {
		id : data
	};
    var width = 650;
    var height = 735;

    open_window(target, url, post_data, width, height);
};

//결제관리창 무통장 팝업
var open_contract_passbook_send_window = function (data) {
    var target = 'contract_sms_passbook_send_window';
    var url = '/admin/contract_sms/contract_sms_insert_passbook_window';
    var post_data = {
		id : data
	};
    var width = 650;
    var height = 535;

    open_window(target, url, post_data, width, height);
};

//공지사항 등록 팝업
var open_notice_send_window = function () {
    var target = 'notice_send_window';
    var url = '/admin/notice/notice_insert_window';
    var post_data = null;
    var width = 1000;
    var height = 600;

    open_window(target, url, post_data, width, height);
};

//공지사항 수정 팝업
var open_notice_modify_send_window = function (data) {
    var target = 'notice_send_window';
    var url = '/admin/notice/notice_modify_window';
    var post_data = {
		id : data
	};
    var width = 1000;
    var height = 600;

    open_window(target, url, post_data, width, height);
};

//1:1문의 답변등록 팝업
var open_notice_info_window = function (notice_id) {
    var target = 'open_notice_info_window';
    var url = '/admin/notice/notice_info_window';
    var post_data = {notice_id : notice_id};
    var width = 1000;
    var height = 500;

    open_window(target, url, post_data, width, height);
};

//견적서 팝업
var open_customer_contract_modify_info_window = function (contract_id, type) {
    var target = 'open_customer_contract_modify_info_window';
    var url = '/admin/contract/contract_modify_info_window';
    var post_data = {
		contract_id : contract_id,
		type : type
		};
    var width = 1200;
    var height = 700;

    open_window(target, url, post_data, width, height);
};

//사용자견적서 팝업
var open_contract_service_info_window = function (contract_id) {
    var target = 'open_contract_service_info_window';
    var url = '/admin/contract/contract_service_info_window';
    var post_data = {
		contract_id : contract_id
		};
    var width = 800;
    var height = 700;

    open_window(target, url, post_data, width, height);
};

//사용자계약서 팝업
var open_contract_info_window = function (contract_id) {
    var target = 'open_contract_info_window';
    var url = '/admin/contract/contract_info_window';
    var post_data = {
		contract_id : contract_id
		};
    var width = 690;
    var height = 700;

    open_window(target, url, post_data, width, height);
};


// 쇼핑몰별 매출통계현황 팝업
var open_statistics_mall_info_window = function (search_data, date_type, start_at, end_at, mall_ids, supplier_ids, sort_field, sort_type, type, amount_type) {
    var target = 'statistics_mall_info_window';
    var url = '/popup/statistics_mall_info_window';
    var post_data = {
        search_data: search_data,
        date_type: date_type,
        start_at: start_at,
        end_at: end_at,
        mall_ids: mall_ids,
        supplier_ids: supplier_ids,
        sort_field: sort_field,
        sort_type: sort_type,
        type: type,
        amount_type: amount_type
    };
    var width = 1200;
    var height = 800;

    open_window(target, url, post_data, width, height);
};

// C/S 등록 팝업
var open_order_customer_service_info_window = function (order_ids) {
    var target = 'order_customer_service_info_window';
    var url = '/popup/order_customer_service_info_window';
    var post_data = {
    	order_ids: order_ids
    };
    var width = 1200;
    var height = 520;

    open_window(target, url, post_data, width, height);
};

// 홈페이지 이동
var open_homepage = function (url) {
    window.open("http://www.facetwo.co.kr" + url, "openWin");
};

// 로그아웃
var logout = function(customer_id, user_type) {
    $.ajax(
        {
        	type: 'POST',
            url: '/login/logout',
            success: function (data) {
                if (data.logout_success) {
                    window.location.href = getBaseUrl() + '/';
                }
            }
        }
    );
};

//쇼핑몰 바로가기
var scm_login = function (mall_user_id) {
    $.ajax(
        {
        	type: 'POST',
            url: '/common/get_mall_user',
            data: {
                id: mall_user_id
            },
            success: function (data) {
                if (!data) {
                    alert('쇼핑몰계정정보가 존재하지 않습니다.');
                    return;
                }
                
                var redirect_mall = [10003, 10006, 10007, 10008, 10011, 10018, 10022, 10023, 10033, 10058, 10065, 10066, 10072, 10074, 10076, 10078, 10088, 10093, 10094, 10095, 10096];
                var post_data = {
                    mall_id: data.mall_id,
                    user_id: data.user_id,
                    user_pwd: data.user_pwd,
                    shop_id: data.shop_id,
                    api_id: data.api_id,
                    shop_url: data.shop_url,
                    etc1: data.etc1,
                    etc2: data.etc2,
                    etc3: data.etc3,
                    etc4: data.etc4,
                    etc5: data.etc5,
                    etc6: data.etc6,
                    etc7: data.etc7,
                    etc8: data.etc8,
                    etc9: data.etc9,
                    etc10: data.etc10
                };

                var target = "scm_login_window_" + data.mall_id;

                if (redirect_mall.indexOf(data.mall_id) >= 0) {
                    target = "scm_auto_login_" + data.mall_id;

                    var url_10003 = "https://ipss.interpark.com/member/login.do?";
                    url_10003 += "_method=loginCase&ipssUserPath=%2Fipss%2Fipssmainscr.do";
                    url_10003 += "&ipssUserQuery=_method%3Dinitial%26_style%3DipssPro%26wid1%3Dwgnb%26wid2%3Dwel_login%26wid3%3Dseller";
                    url_10003 += "&sc.useAppTp=02&sc.isIpss=E&sc.enterEntr=Y&checkCaptchaText=%40IPSS%40&";
                    url_10003 += "sc.memId=" + data.user_id + "&sc.pwd=" + encodeURIComponent(data.user_pwd) + "&oCheckCaptcha=&ordNm=&hp=&ordPw=";

                    var redirect_url = new Array();
                    redirect_url["10003"] = url_10003;
                    redirect_url["10006"] = "https://wing.coupang.com/";
                    redirect_url["10007"] = "https://spc.ticketmonster.co.kr/";
                    redirect_url["10018"] = "https://partner.cjmall.com/websquare/wsengine/uiPath.fo?w2xPath=/ui/syscommon/mainTop.xml";
                    redirect_url["10022"] = "http://po.ssgadm.com/main.ssg";
                    redirect_url["10023"] = "http://splace.akmall.com/wqxml/main.html";
                    redirect_url["10033"] = "http://seller-scm.istyle24.com/default.aspx";
                    redirect_url["10058"] = "https://malladmin.hanssem.com/main.do";
                    redirect_url["10065"] = "https://wpartner.wemakeprice.com/";
                    redirect_url["10066"] = "https://partner.29cm.co.kr/dashboard";
                    redirect_url["10072"] = "http://gdadmin." + data.etc1 + "/base/index.php";
                    redirect_url["10074"] = "https://partner.seoulstore.com/shop_admin/";
                    redirect_url["10078"] = "http://malladmin.benecafe.co.kr/common/poMain";
                    redirect_url["10088"] = "http://gdadmin.hago.kr/provider/base/index.php";
                    redirect_url["10094"] = "https://shop.zigzag.kr/#!/home";
                    redirect_url["10095"] = "https://my.a-bly.com/dashboard";

                    setTimeout(function() {
                        window.open(redirect_url[data.mall_id], 'scm_auto_login_' + data.mall_id);
                    }, 3000);
                }

                var url = '/basic/basic_scm_login/scm_login_window';

                main_open_window(target, url, post_data, null, null, 0);
            }
        }
    );
};

var goods_preview = function (mall_id, mall_product_id, mall_user_id, sub_mall_product_id) {
    var goods_preview_url = [];
    // 옥션
    goods_preview_url[10000] = "http://Itempage3.auction.co.kr/DetailView.aspx?itemno=#{mall_product_id}";
    // 지마켓
    goods_preview_url[10001] = "http://item.gmarket.co.kr/detailview/Item.asp?goodscode=#{mall_product_id}";
    // 11번가
    goods_preview_url[10002] = "http://www.11st.co.kr/product/SellerProductDetail.tmall?method=getSellerProductDetail&prdNo=#{mall_product_id}";
    // 인터파크
    goods_preview_url[10003] = "http://shopping.interpark.com/product/productInfo.do?prdNo=#{mall_product_id}";
    // 스마트스토어
    goods_preview_url[10004] = "http://#{mall_url}/products/#{mall_product_id}";
    // QOO10
    goods_preview_url[10005] = "https://www.#{mall_url}/gmkt.inc/Goods/Goods.aspx?goodscode=#{mall_product_id}";
    // 쿠팡
    goods_preview_url[10006] = "http://www.coupang.com/vp/products/#{sub_mall_product_id}";
    // 티몬
    goods_preview_url[10007] = "http://www.ticketmonster.co.kr/deal/#{mall_product_id}";
    // 카페24
    goods_preview_url[10011] = "http://#{mall_url}/product/detail.html?product_no=#{sub_mall_product_id}";
    // 고도몰
    goods_preview_url[10012] = "http://#{mall_url}/shop/goods/goods_view.php?goodsno=#{mall_product_id}";
    // 메이크샵
    goods_preview_url[10013] = "http://#{mall_url}/shop/shopdetail.html?branduid=#{mall_product_id}";
    // 퍼스트몰
    goods_preview_url[10014] = "http://#{mall_url}/product/#{mall_product_id}";
    // GS SHOP
    goods_preview_url[10015] = "http://www.gsshop.com/prd/prd.gs?prdid=#{mall_product_id}";
    // Hmall
    goods_preview_url[10016] = "http://www.hyundaihmall.com/front/pda/itemPtc.do?preview=true&slitmCd=#{mall_product_id}";
    // 더현대닷컴
    goods_preview_url[10017] = "http://www.thehyundai.com/front/pda/itemPtc.thd?slitmCd=#{mall_product_id}";
    // CJmall
    goods_preview_url[10018] = "http://display.cjmall.com/p/item/#{mall_product_id}";
    // 롯데홈쇼핑
    goods_preview_url[10019] = "http://www.lotteimall.com/goods/viewGoodsDetail.lotte?goods_no=#{mall_product_id}";
    // 롯데닷컴
    goods_preview_url[10020] = "http://www.lotte.com/goods/viewGoodsDetail.lotte?goods_no=#{mall_product_id}";
    // 롯데백화점
    goods_preview_url[10021] = "http://www.ellotte.com/goods/viewGoodsDetail.lotte?goods_no=#{mall_product_id}";
    // 신세계몰
    goods_preview_url[10022] = "http://www.ssg.com/item/itemView.ssg?itemId=#{mall_product_id}";
    // AK몰
    goods_preview_url[10023] = "http://www.akmall.com/goods/GoodsDetail.do?goods_id=#{mall_product_id}";
    // NS홈쇼핑
    goods_preview_url[10024] = "http://www.nsmall.com/ProductDisplay?partNumber=#{mall_product_id}";
    // 홈앤쇼핑
    goods_preview_url[10025] = "http://www.hnsmall.com/display/goods.do?goods_code=#{mall_product_id}";
    // 공영홈쇼핑
    goods_preview_url[10027] = "https://www.gongyoungshop.kr/goods/selectGoodsDetail.do?prdId=#{mall_product_id}";
    // 갤러리아
    goods_preview_url[10028] = "http://www.galleria.co.kr/item/showItemDtl.do?item_id=#{mall_product_id}";
    // 하프클럽
    goods_preview_url[10031] = "http://www.halfclub.com/Detail?PrStCd=#{mall_product_id}&ColorCd=ZZ9";
    // 패션플러스
    goods_preview_url[10032] = "http://www.fashionplus.co.kr/mall/goods/goods.asp?goods_id=#{mall_product_id}";
    // 아이스타일24
    goods_preview_url[10033] = "http://www.istyle24.com/Display/ProductDetail.aspx?ProductNo=#{mall_product_id}";
    // LF몰
    goods_preview_url[10034] = "http://www.lfmall.co.kr/product.do?cmd=getProductDetail&PROD_CD=#{mall_product_id}"
    // 무신사
    goods_preview_url[10035] = "https://store.musinsa.com/app/product/detail/#{mall_product_id}/0";
    // 위즈위드
    goods_preview_url[10036] = "http://www.wizwid.com/CSW/handler/wizwid/kr/MallProduct-Start?AssortID=#{mall_product_id}&preview=Y";
    // 플레이어
    goods_preview_url[10037] = "http://www.player.co.kr/v3/category/detail.php?goods_no=#{mall_product_id}&goods_sub=0";
    // 텐바이텐
    goods_preview_url[10040] = "http://www.10x10.co.kr/shopping/category_prd.asp?itemid=#{mall_product_id}";
    // 다이소몰
    goods_preview_url[10041] = "http://daisomall.co.kr/shop/goods_view.php?id=#{mall_product_id}";
    // 바보사랑
    goods_preview_url[10042] = "http://www.babosarang.co.kr/product/product_detail.php?product_no=#{mall_product_id}";
    // 1300K
    goods_preview_url[10043] = "http://www.1300k.com/shop/goodsDetail.html?f_goodsno=#{mall_product_id}";
    // 교보
    goods_preview_url[10044] = "http://gift.kyobobook.co.kr/ht/product/detail?barcode=#{mall_product_id}";
    // 후추통
    goods_preview_url[10052] = "http://www.hoochootong.com/group/Prod_V.php?p_id=#{mall_product_id}";
    // 이랜드몰
    goods_preview_url[10053] = "http://www.elandmall.com/goods/initGoodsDetail.action?goods_no=#{mall_product_id}";
    // GS리테일
    goods_preview_url[10055] = "http://www.gsfresh.com/eretail.product.prodDetail.dev?prodId=#{mall_product_id}";
    // 제로투세븐
    goods_preview_url[10056] = "http://www.0to7.com/pms/product/detail?productId=#{mall_product_id}";
    // W컨셉
    goods_preview_url[10057] = "http://www.wconcept.co.kr/Shop/ViewProduct.cshtml?itemcd=#{mall_product_id}";
    // 한샘
    goods_preview_url[10058] = "http://mall.hanssem.com/goods/goodsDetailMall.do?gdsNo=#{mall_product_id}";
    // 가방팝
    goods_preview_url[10061] = "http://www.gabangpop.co.kr/app/product/detail/#{mall_product_id}/0";
    // 크루비
    goods_preview_url[10062] = "https://crewbi.com/app/product/detail/#{mall_product_id}/0";
    // 스마트웰
    goods_preview_url[10063] = "http://www.buyis.co.kr/product/detail/#{mall_product_id}";
    // 위메프2.0
    goods_preview_url[10065] = "https://front.wemakeprice.com/product/#{mall_product_id}";
    // 29CM
    goods_preview_url[10066] = "https://www.29cm.co.kr/product/#{mall_product_id}";
    // 패션포유
    goods_preview_url[10069] = "http://www.fashion4you.co.kr/shop/shopdetail.html?branduid=#{mall_product_id}";
    // 고도몰5
    goods_preview_url[10072] = "http://#{mall_url}/goods/goods_view.php?goodsNo=#{mall_product_id}";
    // 서울스토어
    goods_preview_url[10074] = "https://www.seoulstore.com/products/#{mall_product_id}/detail?product_id=#{mall_product_id}";
    // ALAND
    goods_preview_url[10075] = "https://a-land.co.kr/shop/goods_view.php?id=#{mall_product_id}";
    // 어라운드더코너
    goods_preview_url[10076] = "https://www.aroundthecorner.com/shop/view.php?index_no=#{mall_product_id}";
     // 힙합퍼
    goods_preview_url[10077] = "https://www.hiphoper.com/item/#{mall_product_id}";
    // OCO
    goods_preview_url[10079] = "https://www.ocokorea.com/shop/goods/product_view.do?pid=#{sub_mall_product_id}";
    // 스타일쉐어
    goods_preview_url[10080] = "https://www.stylesha.re/goods/#{mall_product_id}";
    // 에이티브
    goods_preview_url[10082] = "https://ative.kr/shop/goods_view.php?id=#{mall_product_id}";
    // 펑펑몰
    goods_preview_url[10083] = "http://www.pungpungmall.com/shop/shopdetail.html?branduid=#{mall_product_id}";
    // 마리오몰
    goods_preview_url[10087] = "http://www.mariomall.co.kr/Detail?PCode=#{mall_product_id}";
    // HAGO
    goods_preview_url[10088] = "http://www.hago.kr/goods/goods_view.php?goodsNo=#{mall_product_id}";
    // 브랜디
    goods_preview_url[10089] = "https://www.brandi.co.kr/products/#{mall_product_id}";
    // 하이버
    goods_preview_url[10090] = "https://www.hiver.co.kr/products/#{mall_product_id}";
    // 인터파크비즈마켓
    goods_preview_url[10092] = "http://ebiz.interparkb2b.co.kr/goods/content_readonly_mallinmall.asp?guid=#{mall_product_id}";
    // 에이블리
    goods_preview_url[10095] = "https://a-bly.com/app/goods/preview.php?sno=#{mall_product_id}";

    var sub_malls = ['10006', '10011'];

    if (sub_malls.indexOf('' + mall_id + '') > -1) {
        $.ajax({
        	type: 'POST',
            url: '/common/get_sub_mall_product_id',
            data: {
                mall_product_id: mall_product_id,
                mall_id: mall_id
            },
            success: function (data) {
                if(!data) {
                    alert("쇼핑몰 상품코드 매칭, 승인 후 확인이 가능합니다.");
                    return;
                }

                var sub_mall_product_id = data.sub_mall_product_id;
                if (sub_mall_product_id == '') {
                    alert('승인 후 확인이 가능합니다.');
                } else {
                    var preview_url = goods_preview_url[mall_id];

                    if (preview_url) {
                        preview_url = preview_url.replace(/#\{mall_product_id\}/g, mall_product_id);
                        preview_url = preview_url.replace(/#\{sub_mall_product_id\}/g, sub_mall_product_id);

                        if (mall_user_id) {
                            get_mall_users(mall_id, mall_user_id).then(function (data) {
                                preview_url = preview_url.replace(/#\{mall_url\}/g, data.length > 0 ? data[0].etc1 : '');

                                window.open(preview_url);
                            });
                        } else {
                            window.open(preview_url);
                        }
                    }
                }
            }
        });
    } else {
        var preview_url = goods_preview_url[mall_id];

        if (preview_url) {
            if (mall_id == 10079) { // OCO
                var pid = sub_mall_product_id == '' ? mall_product_id : sub_mall_product_id;
                preview_url = preview_url.replace(/#\{sub_mall_product_id\}/g, pid);
            } else {
                preview_url = preview_url.replace(/#\{mall_product_id\}/g, mall_product_id);
                preview_url = preview_url.replace(/#\{sub_mall_product_id\}/g, sub_mall_product_id);
            }

            if (mall_user_id) {
                get_mall_users(mall_id, mall_user_id).then(function (data) {
                    var mall_url = "";

                    for(var i = 0; i<data.length; i++){
                        if(data[i].id == mall_user_id){
                            mall_url = data[i].etc1;
                            break;
                        }
                    }

                    preview_url = preview_url.replace(/#\{mall_url\}/g, mall_url);

                    window.open(preview_url);
                });
            } else {
                window.open(preview_url);
            }
        }
    }
};

var open_delivery_tracking_window = function (delivery_id, invoice) {
    window.open("/order/order_send/delivery_tracking_window?delivery_id=" + delivery_id + "&invoice=" + invoice);
};

// 메인 서비스 공지사항/협력사 공지사항/온라인 문의 - 공지사항으로 이동
var go_notice_list = function (header_category) {
    var url = '/service/service_notify';
    var post_data = {
        header_category: header_category
    };

    href_page(url, post_data);
};

// 메인 주문처리/클레임현황 - 주문확인처리로 이동
var go_order_matching = function (order_status) {
    var url = '/order/order_matching?fragment=0';
    var post_data = {
		menu_id : 303,
		search : true,
		date_type : 'created_at',
        order_statuses : order_status ,
        start_at:  moment().add(-1, 'month').format("YYYY-MM-DD"),
        end_at:  moment().format("YYYY-MM-DD")
    };
	
    href_page(url, post_data);
};

// 메인 주문처리/상품판매현황 - 상품조회/수정 이동
var go_product_list = function (sale_status) {
    var url = '/product/product_list?fragment=0';
    var post_data = {
		menu_id : 203,
		search : true,
		date_type : 'updated_at',
        sale_status : sale_status ,
        start_at:  moment().add(-1, 'month').format("YYYY-MM-DD"),
        end_at:  moment().format("YYYY-MM-DD")
    };
	
    href_page(url, post_data);
};

// 메인 문의현황 - c/s관리>문의/답변 이동
var go_order_claim = function (inquiry_status) {
    var url = '/order/order_claim_cs?fragment=1';
    var post_data = {
		menu_id : 305,
		search : true,
		date_type : 'created_at',
        inquiry_status : inquiry_status ,
        start_at:  moment().add(-1, 'month').format("YYYY-MM-DD"),
        end_at:  moment().format("YYYY-MM-DD")
    };
	
    href_page(url, post_data);
};

// 메인 상품별 매출(수량)내역 - 기간별 이동
var go_admin_statisticsSales = function (statistics_status, kind) {
    var url = '/admin/statistics_sales?fragment='+statistics_status;
	if( kind == 'product'){
		 var period = $("#product_select option:selected").val();
	} else if( kind == 'mall'){
		 var period = $("#mall_select option:selected").val();
	} else if( kind == 'option'){
		 var period = $("#option_select option:selected").val();
	}
   
	var post_data = {
        menu_id : 504,
		search : true,
        start_at:  moment().add(-period, 'month').format("YYYY-MM-DD"),
        end_at:  moment().format("YYYY-MM-DD")
    };

    href_page(url, post_data);
};

// 메인 상품판매현황 - 주문확인처리로 이동
var go_s_order_list = function (product_id, start_at, end_at) {
    var url = '/order/order_list';
    var post_data = {
        search_field: 'product_id',
        search_text: product_id ? product_id : '',
        order_status: '1442,1443,1444,1445,1446,1447,1448,1450,1451,1453,1454,1455,1528,1529,1530',
        start_at: start_at ? start_at : moment().add(-1, 'month').format("YYYY-MM-DD"),
        end_at: end_at ? end_at : moment().format("YYYY-MM-DD")
    };

    href_page(url, post_data);
};

// 메인 문의현황 - 문의사항답변전송으로 이동
var go_inquiry_list = function (inquiry_status, start_at, end_at) {
    var url = '/order/order_inquiry';
    var post_data = {
        inquiry_status: inquiry_status,
        start_at: start_at ? start_at : moment().add(-1, 'month').format("YYYY-MM-DD"),
        end_at: end_at ? end_at : moment().format("YYYY-MM-DD")
    };

    href_page(url, post_data);
};

// 고객사 바로가기
var admin_setting = function () {
    var admin_customer_id = $('#customer_admin_shortcut').val();

    if (admin_customer_id > 0) {
        $.ajax(
            {
            	type: 'POST',
                url: '/login/login',
                data: {
                    ADMIN_CUSTOMER_ID: admin_customer_id
                }
            }
        ).then(
            function (data) {
                if(data.login_success || !data.error) {
                    window.location.reload(true);
                }
            });
    }
};

// 오늘, 어제, 이번달, 3개월, 6개월, 1년
var date_setting = function (term, day_type) {
    var obj = {
        start_at: $('#dt_start_at'),
        end_at: $('#dt_end_at')
    };

    obj.end_at.datepicker('setDate', moment().toDate());

    if (term == 's') { // 이번달
        obj.start_at.datepicker('setDate', moment().startOf('month').toDate());
    } else if (term == 'y') { // 어제
        obj.start_at.datepicker('setDate', moment().add(-1, day_type).toDate());
        obj.end_at.datepicker('setDate', moment().add(-1, day_type).toDate());
    } else if (term == 'l') { // 저번달
        obj.start_at.datepicker('setDate', moment().add(-1, day_type).startOf('month').toDate());
        obj.end_at.datepicker('setDate', moment().add(-1, day_type).endOf('month').toDate());
    } else {
        obj.start_at.datepicker('setDate', moment().add(-(term), day_type).toDate());
    }
};

// 메뉴 바로가기
var shortcuts = function (menu_id) {
    $.ajax(
        {
            type: 'POST',
            url: '/common/get_menu',
            data: {
                menu_id: menu_id ? menu_id : $('#sel_shortcuts').val()
            },
            success: function (data) {
                window.location.href = getBaseUrl() + data.path;
            }
        }
    );
};

var get_middle_customer_categories = function (code_l) {
    var deferred = $.Deferred();

    $.ajax({
        type: 'POST',
        url: '/common/get_middle_customer_categories',
        data: {
            code_l: code_l
        },
        success: function (data) {
            deferred.resolve(data);
        }
    });

    return deferred.promise();
};

var get_small_customer_categories = function (code_l, code_m) {
    var deferred = $.Deferred();

    $.ajax({
        type: 'POST',
        url: '/common/get_small_customer_categories',
        data: {
            code_l: code_l,
            code_m: code_m
        },
        success: function (data) {
            deferred.resolve(data);
        }
    });

    return deferred.promise();
};

var get_detail_customer_categories = function (code_l, code_m, code_s) {
    var deferred = $.Deferred();

    $.ajax({
        type: 'POST',
        url: '/common/get_detail_customer_categories',
        data: {
            code_l: code_l,
            code_m: code_m,
            code_s: code_s
        },
        success: function (data) {
            deferred.resolve(data);
        }
    });

    return deferred.promise();
};

//카테고리
var select_category = function (type) {
    var deferred = $.Deferred();

    switch (type) {
        case 'large': {
            get_middle_customer_categories(
                $('#sel_l_category').val()
            ).then(function (data) {
                var content = '<option value="">중카테고리</option>' + data.map(function (d) {
                        return '<option value="' + d.code_m + '">' + d.name_m + '</option>';
                    }).join('');

                $('#sel_m_category').html(content);
                $('#sel_s_category').html('<option value="">소카테고리</option>');
                $('#sel_d_category').html('<option value="">세카테고리</option>');

                deferred.resolve();
            });

            break;
        }
        case 'middle': {
            get_small_customer_categories(
                $('#sel_l_category').val(),
                $('#sel_m_category').val()
            ).then(function (data) {
                var content = '<option value="">소카테고리</option>' + data.map(function (d) {
                        return '<option value="' + d.code_s + '">' + d.name_s + '</option>';
                    }).join('');

                $('#sel_s_category').html(content);
                $('#sel_d_category').html('<option value="">세카테고리</option>');

                deferred.resolve();
            });

            break;
        }
        case 'small': {
            get_detail_customer_categories(
                $('#sel_l_category').val(),
                $('#sel_m_category').val(),
                $('#sel_s_category').val()
            ).then(function (data) {
                var content = '<option value="">세카테고리</option>' + data.map(function (d) {
                        return '<option value="' + d.code_d + '">' + d.name_d + '</option>';
                    }).join('');

                $('#sel_d_category').html(content);

                deferred.resolve();
            });

            break;
        }
    }

    return deferred.promise();
};

var select_mall = function () {
	var deferred = $.Deferred();
	
	var mall_id = $('#sel_mall').val();
	
	if (mall_id) {
        get_mall_users(mall_id).then(
            function (data) {
                var selector = $('#sel_mall_user');
                var content = '<option value="" selected="true">쇼핑몰아이디</option>';

                content += data.map(function (d) {
                    return '<option value="' + d.id + '">' + d.user_id + '</option>';
                }).join('');

                selector.html(content);
                
                deferred.resolve();
            }
        );
    } else {
        $('#sel_mall_user').html('<option value="" selected="true">쇼핑몰아이디</option>');
        
        deferred.resolve();
    }
	
	return deferred.promise();
};

var select_invoice_band = function (delivery, start_invoice, end_invoice, count) {
	var deferred = $.Deferred();
	
	if (delivery) {
        $.ajax({
        	type: 'POST',
        	url: '/common/get_invoice_band',
        	data: {
        		delivery: delivery
        	},
        	success: function (data) {
        		if (data) {
        			var start = parseInt(data.start_invoice);
        			var end = parseInt(data.end_invoice);
        			
        			start_invoice.val(data.start_invoice);
        			
        			if (end_invoice) {
        				end_invoice.val(data.end_invoice);
        			}
        			
        			if (count) {
        				count.val(end - start);
        			}
        		} else {
        			start_invoice.val('');
        			
        			if (end_invoice) {
        				end_invoice.val('');
        			}
        			
        			if (count) {
        				count.val('');
        			}
        		}
        		
        		deferred.resolve();
        	}
        });
    } else {
    	start.val('');
		
		if (end) {
			end.val('');
		}
		
		if (count) {
			count.val('');
		}
    	
    	deferred.resolve();
    }
	
	return deferred.promise();
};

var enter_search = function (event) {
    var keyCode = event.which || event.keyCode;
    if (keyCode === 13) {
        $('#search_form').submit();
    }
};

var calc_mall_commission = function (product_supply_price, mall_commission, total_price, payment_price, quantity, supply_price, settlement_price) {
    var result = {
        supply_price: supply_price,
        payment_price: payment_price == 0 ? total_price : payment_price,
        settlement_price: settlement_price
    };

    if (supply_price == 0) {
        if (mall_commission == 0) {
            result.supply_price = product_supply_price;
        } else {
            result.supply_price = total_price == 0 ? 0 : Math.round((total_price/quantity) * ((100-mall_commission)/100));
        }
    }

    if (settlement_price == 0) {
        if (mall_commission == 0) {
            result.settlement_price = result.payment_price;
        } else {
            result.settlement_price = Math.round(result.payment_price * ((100-mall_commission)/100));
        }
    }

    return result;
};

var href_cafe24_order_detail = function (mall_order_id) {
    var post_data = {
        order_id: mall_order_id
    };

    var url = 'https://andar01.cafe24.com/admin/php/shop1/s_new/order_detail.php';

    open_window('cafe24_order_detail_info_window', url, post_data, 1024, 768, 'GET');
};

var href_makeshop_order_detail = function (mall_order_id) {
    var post_data = {
        ordernum: mall_order_id,
        resize: 'OK'
    };

    var url = 'https://special220.makeshop.co.kr/makeshop/oomanager/oo_detail.html';

    open_window('oo_detail', url, post_data, 1100, 800, 'GET');
};

var pdf_download = function (pdf_url, download_file_name) {
    $.ajax({
        url: '/common/pdf_download',
        type: 'POST',
        xhrFields: {
            responseType: 'blob'
        },
        data: {
            url: pdf_url
        },
        success: function(data) {
            var link = document.createElement('a');
            link.href = window.URL.createObjectURL(data);
            link.download = download_file_name;
            link.click();
        }
    });
};

var toggleCheck = function (toggleObj) {
	if ($(toggleObj).prop("checked")) {
		$($(toggleObj).val()).prop("checked", true);
	} else {
		$($(toggleObj).val()).prop("checked", false);
	}
}

var href_pagination = function (no) {
	var form = $('#pagination_form');
	
	form.find('[name="page_no"]').val(no);
	form.attr('action', $('#search_form').attr('action'));
	form.submit();
};

$(document).ready(function () {
	//nav
	/*$(".nav li").click(function(){
		$(this).siblings().removeClass('active');
		$(this).addClass('active');
	});*/
	
	$('.nav li').hover(function(){
		$(this).find('.innerNav').stop().slideDown(250);
		$(this).addClass('on');
	}, function(){
		$(this).find('.innerNav').stop().hide(0);
		$(this).removeClass('on');
	});

	//tab - only jquery
	$('.tab_content:first-child').show();
	$('.tab_tit').bind('click', function(e){
		$this=$(this);
		
		if ($this.hasClass('active')) return;
		
		$tabs=$this.parent().next();
		$target=$($this.data('target')); // get the target from data attribute
		$this.siblings().removeClass('active');
		$target.siblings().css('display','none')
		$this.addClass('active');
		$target.fadeIn('fast');
		
		if (!$(this).parent().hasClass('tab_sm_titWrap')) {
			var path = window.location.pathname + '?fragment=' + $(this).data('value');
			var menu_id = $('#MENU_ID').val();
			
			href_menu(path, menu_id);
		}
	});
	
	//$('.tab_tit.active').trigger('click');
	
	// checkbox click시 tr 선택 
	$(".tr_selectAnd tbody input:checkbox").click(function () {
		var tr = $(this).closest("tr");
		
		tr.toggleClass("active");
		
		if (tr.hasClass("active")) {
			tr.find("input:checkbox:first").prop('checked', true);
		} else {
			tr.find("input:checkbox:first").prop('checked', false);
		}
	});

	// slimScroll
	if($('.slimScroll').length>0){
		$('.slimScroll').slimScroll({ 
			size:'8px',
			height:'100%',
			opacity:.1,
			alwaysVisible:true,
			axis:"both",
			railVisible:true,
			railOpacity:0.05,
			railBorderRadius:'0'
		});
	}
	
	// 맨위로 scrollTop
	var scrollTop = $(".scrollTop");
	var topPos=null;
	$(window).scroll(function(){
		topPos=$(this).scrollTop();
		if(topPos > 100){
			$(scrollTop).addClass('active');
		}else{
			$(scrollTop).removeClass('active');
		}
	});
    
	$(scrollTop).click(function(){
		$('html, body').animate({scrollTop:0}, 300);
		return false;
	});
	
	/*if($('.scrollTop').length>0){
		$('.scrollTop').fixTo('#container', {top: 0, useNativeSticky:true});
	}*/
    
	//sort_table
	if($('.sort_table').length>0){
		$('.sort_table').tablesorter({
			widgets        : ['zebra', 'columns'],
			usNumberFormat : false,
			sortReset      : true,
			sortRestart    : true
		});
	}
	
	// 비활성화, 클릭 막기
	$(".dis, .disabled, .no_click").click(function(e){
		e.preventDefault();
	});

	// 즐겨찾기 퀵메뉴 동작
	$(".btn-fold").click(function() {
	    $(".quick-wrap").toggle("slow");
	});
	
	// 이미지 확대 출력
	$(".img_mini").each(function (index) {	
		$(this).click(function () {	
			if($(this).children(".img_original").css("display") == "none") {
				$(".img_mini").children(".img_original").css("display", "none");
				$(this).children(".img_original").css("display", "block");
			} else {
				$(this).children(".img_original").css("display", "none");
			}
		});
	});
});