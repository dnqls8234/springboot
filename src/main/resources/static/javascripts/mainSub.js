function checkFileType(filePath) {
		var fileFormat = filePath.split(".");
		if (fileFormat.indexOf("xls") > -1) {
			return true;
		} else if (fileFormat.indexOf("xlsx") > -1) {
			return true;
		} else {
			return false;
		}
	}

	function check() {
		var file = $("#excel").val();
		if (file == "" || file == null) {
			alert("파일을 선택");
			return false;
		} else if (!checkFileType(file)) {
			alert("엑셀 파일만 업로드");
			return false;
		}
		var fileFormat = file.split(".");
		var fileType = fileFormat[1];
		if (confirm("업로드 하시겠습니까?")) {
			$("#excelUpForm").attr("action", "/membersub/compExcelUpload");
			var options = {
				success : function(data) {
					alert(data);
					location.reload();
					$("#ajax-content").html(data);
				},
				type : "POST",
				data : {
					"excelType" : fileType
				}
			};
			$("#excelUpForm").ajaxSubmit(options);
		} else {
			location.reload();
		}
	}

	function getBaseUrl() {
		var pathArray = location.href.split('/');
		var protocol = pathArray[0];
		var domain = pathArray[2];

		var url = protocol + '//' + domain;

		return url;
	}

	var download_excel = function() {
		if (!confirm("다운로드 하시겟습니까?")) {
			return false;
		}

		var list_range = $("#sel_list_range").val();

		var url = '/membersub/downloadExcel';
		var params = $('#excel_form').serialize();

		filedownload(url, params);
	}

	var filedownload = function(url, params, successCallback) {

		if (!successCallback) {
			successCallback = function(url) {
			};
		}

		$.fileDownload(getBaseUrl() + url, {
			httpMethod : 'POST',
			data : params,
			successCallback : successCallback,
			failCallback : function() {
				alert("파일다운로드실패");
			}
		}).done(function() {
		});
	};

	var excelbuttom = function() {
		$("#excel").click();
	}

	var paging = function(no, page) {
		var form = $("#paging");

		if (page == 'next') {
			if (no == null) {
				no = 1;
			}
			no++;
		} else if (page == 'pre') {
			if (no == null) {
				no = 1;
			}
			if (no > 1) {
				no--;
			}
		}

		form.find('[name="page_no"]').val(no);
		form.submit();

	}

	function sortTable(n) {
		var table, updown, rows, switching, o, x, y, shouldSwitch, dir, switchcount = 0;
		table = document.getElementById("inventory");
		switching = true;
		dir = "asc";

		while (switching) {
			switching = false;
			rows = table.getElementsByTagName("tr");

			for (o = 1; o < (rows.length - 1); o++) {
				shouldSwitch = false;
				x = rows[o].getElementsByTagName("td")[n];
				y = rows[o + 1].getElementsByTagName("td")[n];

				if (dir == "asc") {
					if (x.innerHTML.toLowerCase() > y.innerHTML.toLowerCase()) {
						var th = table.getElementsByTagName("th");
						var resetii = table.getElementsByTagName("i");
						for (var j = 0; j < resetii.length; j++) {
							resetii[j].style.visibility = '';
						}
						var ii = th[n].getElementsByTagName("i");
						ii[1].style.visibility = 'hidden';
						ii[0].style.visibility = '';
						shouldSwitch = true;
						break;

					}

				} else if (dir == "desc") {
					if (x.innerHTML.toLowerCase() < y.innerHTML.toLowerCase()) {
						var th = table.getElementsByTagName("th");
						var resetii = table.getElementsByTagName("i");
						for (var j = 0; j < resetii.length; j++) {
							resetii[j].style.visibility = '';
						}
						var ii = th[n].getElementsByTagName("i");
						ii[1].style.visibility = '';
						ii[0].style.visibility = 'hidden';
						shouldSwitch = true;
						break;

					}
				}
			}

			if (shouldSwitch) {
				rows[o].parentNode.insertBefore(rows[o + 1], rows[o]);
				switching = true;
				switchcount++;
			} else {
				if (switchcount == 0 && dir == "asc") {
					dir = "desc";
					switching = true;

				}
			}
		}
	}

	function printTime() {
		var clock = document.getElementById("clock");
		var week = new Array('일', '월', '화', '수', '목', '금', '토');

		var now = new Date();
		clock.innerHTML = (now.getMonth() + 1) + "월 " + now.getDate() + "일  ["
				+ week[now.getDay()] + "] " + now.getHours() + ":"
				+ now.getMinutes() + ":" + now.getSeconds();
		setTimeout("printTime()", 1000);
	}
	window.onload = function() {
		printTime();
	};

	$(document).ready(function() {
//		$('span[class=scphone]').each(function() {
//			var text = $(this).text();
//			text = text.substr(0, text.length - 2) + '**';
//
//			$(this).text(text);
//		});
//
//		$("#modal_check_btn").click(function() {
//			$("#modal").css("display", "none");
//			checkPasswd();
//		});
//		
//		$("#modal_layer").click(function(){
//			$("#modal").css("display", "none");
//		})
		
	})

//	var show = function(e, id) {
//
//		$("#modal").css("display", "block");
//
//		$("#phoneRow").val(e);
//		$("#phoneUserNo").val(id);
//
//	}
//
//	var checkPasswd = function() {
//
//		var e = $("#phoneRow").val();
//		var id = $("#phoneUserNo").val();
//		var passwd = $("#passwd").val();
//		var user_id = $("input[name='user_id']").val();
//		var user_no = $("input[name='user_no']").val();
//
//		$.ajax({
//			method : 'POST',
//			url : '/membersub/securityPhone',
//			data : {
//				id : id,
//				passwd : passwd,
//				user_id : user_id,
//				user_no : user_no
//			},
//			success : function(data) {
//				$("#scphone" + e).text(data);
//				$("#phoneRow").val('');
//				$("#phoneUserNo").val('');
//				$("#passwd").val('');
//			},
//			error : function(data) {
//				$("#phoneRow").val('');
//				$("#phoneUserNo").val('');
//				$("#passwd").val('');
//				alert("비밀번호가 잘못되었습니다.");
//			}
//
//		})
//
//	}
	
	
	$(function(){
			jQuery.datetimepicker.setLocale('kr');
			$('#datetimepicker2').datetimepicker({
	            format:'Y.m.d',
	            lang:'ko',
	            timepicker:false,
	
				defaultDate:'+1970.01.01', // it's my birthday
				timepickerScrollbar:false,
	            
	            onChangeDateTime:function(dp,$input){
	                $('#dateitem').val($input.val());
	            }
	        });//datepicker end
		});