Default.aspx - Demo web page with jqxGrid and "Export to Excel" button. When the button is clicked, a Form with the Grid's data is submitted. 
Default.aspx.cs - It handles the submitted Grid data and creates ".xls" file.

Note: In some cases due to security restraints, you may need to set ValidateRequest="false" on your web page and you may need to set requestValidationMode="2.0" to httpRuntime in the project’s Web.config