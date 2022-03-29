var screen = document.documentElement;

function openFullscreen() {
	if (screen.requestFullscreen) {
		screen.requestFullscreen();
	} else if (screen.webkitRequestFullscreen) { /* Safari */
		screen.webkitRequestFullscreen();
	} else if (screen.msRequestFullscreen) { /* IE11 */
		screen.msRequestFullscreen();
	}
}

function closeFullscreen() {
	if (document.exitFullscreen) {
		document.exitFullscreen();
	} else if (document.webkitExitFullscreen) { /* Safari */
		document.webkitExitFullscreen();
	} else if (document.msExitFullscreen) { /* IE11 */
		document.msExitFullscreen();
	}
}


//function fnExcelReport(id, title) {
//	var tab_text = '<html xmlns:x="urn:schemas-microsoft-com:office:excel">';
//	tab_text = tab_text + '<head><meta http-equiv="content-type" content="application/vnd.ms-excel; charset=UTF-8">';
//	tab_text = tab_text + '<xml><x:ExcelWorkbook><x:ExcelWorksheets><x:ExcelWorksheet>'
//	tab_text = tab_text + '<x:Name>Test Sheet</x:Name>';
//	tab_text = tab_text + '<x:WorksheetOptions><x:Panes></x:Panes></x:WorksheetOptions></x:ExcelWorksheet>';
//	tab_text = tab_text + '</x:ExcelWorksheets></x:ExcelWorkbook></xml></head><body>';
//	tab_text = tab_text + "<table border='1px'>";
//	var exportTable = $('#' + id).clone();
//	exportTable.find('input').each(function (index, elem) { $(elem).remove(); });
//	tab_text = tab_text + exportTable.html();
//	tab_text = tab_text + '</table></body></html>';
//	var data_type = 'data:application/vnd.ms-excel';
//	var ua = window.navigator.userAgent;
//	var msie = ua.indexOf("MSIE ");
//	var fileName = title + '.xls';
//	//Explorer 환경에서 다운로드
//	if (msie > 0 || !!navigator.userAgent.match(/Trident.*rv\:11\./)) {
//		if (window.navigator.msSaveBlob) {
//			var blob = new Blob([tab_text], {
//				type: "application/csv;charset=utf-8;"
//			});
//			navigator.msSaveBlob(blob, fileName);
//		}
//	} else {
//		var blob2 = new Blob([tab_text], {
//			type: "application/csv;charset=utf-8;"
//		});
//		var filename = fileName;
//		var elem = window.document.createElement('a');
//		elem.href = window.URL.createObjectURL(blob2);
//		elem.download = filename;
//		document.body.appendChild(elem);
//		elem.click();
//		document.body.removeChild(elem);
//	}
//}
//
//$(function(){
//
//	/* 엑셀 다운로드 */
//	$('.btn-exdownload').click( function(){
//		var title = $('.ex-title').text();
//		fnExcelReport('ex-table', title);
//	});
//
//});
