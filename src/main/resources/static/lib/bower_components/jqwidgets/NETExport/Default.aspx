<%@ Page Title="Home Page" Language="C#" AutoEventWireup="true" CodeBehind="Default.aspx.cs" Inherits="jQWidgets._Default" ValidateRequest="false" %>

<!DOCTYPE html>
<html lang="en">
<head id="Head1" runat="server">
    <title></title>
    <link type="text/css" rel="stylesheet" href="Content/jqwidgets/jqx.base.css" />
    <link type="text/css" rel="stylesheet" href="Content/jqwidgets/jqx.energyblue.css" />
    <script type="text/javascript" src="Scripts/jquery-1.8.2.min.js"></script>
    <script type="text/javascript" src="Scripts/jqwidgets/jqx-all.js"></script>
    <script type="text/javascript" src="Scripts/jqwidgets/generatedata.js"></script>
    <script type="text/javascript">
        $(document).ready(function () {
            var data = generatedata(10);
            var source = {
                localdata: data,
                datafields: [{
                    name: 'firstname',
                    type: 'string'
                }, {
                    name: 'lastname',
                    type: 'string'
                }, {
                    name: 'productname',
                    type: 'string'
                }, {
                    name: 'date',
                    type: 'date'
                }, {
                    name: 'quantity',
                    type: 'number'
                }, {
                    name: 'price',
                    type: 'number'
                }],
                datatype: "array"
            };

            var adapter = new $.jqx.dataAdapter(source);
            $("#jqxgrid").jqxGrid({
                width: 500,
                theme: 'energyblue',
                height: 300,
                source: adapter,
                sortable: true,
                columns: [{
                    text: 'First Name',
                    datafield: 'firstname',
                    width: 90
                }, {
                    text: 'Last Name',
                    datafield: 'lastname',
                    width: 90
                }, {
                    text: 'Product',
                    datafield: 'productname',
                    width: 170
                }, {
                    text: 'Order Date',
                    datafield: 'date',
                    width: 160,
                    cellsformat: 'dd-MMMM-yyyy'
                }, {
                    text: 'Quantity',
                    datafield: 'quantity',
                    width: 80,
                    cellsalign: 'right'
                }, {
                    text: 'Unit Price',
                    datafield: 'price',
                    cellsalign: 'right',
                    cellsformat: 'c2'
                }]
            });

            $('#fileName').jqxInput({ theme: 'energyblue', height: 25 });

            $('#exportToExcel').jqxButton({ theme: 'energyblue', height: 25 });

            $('#exportToExcel').click(function () {
                exportInfo = $('#jqxgrid').jqxGrid('exportdata', 'xls');
                $('#gridData').val('');
                $('#gridData').val(exportInfo);
            });
        });
    </script>
</head>
<body>
    <div id="jqxgrid"></div>
    <form id="form" action="Default.aspx" method="post">
        <input type="hidden" id="gridData" name="gridData" />
        <label for="fileName">File name:</label>
        <input type="text" id="fileName" name="fileName" />
        <input type="submit" id="exportToExcel" value="Export to Excel" style="margin-top: 20px; margin-left: 10px;" />
    </form>
</body>
</html>
