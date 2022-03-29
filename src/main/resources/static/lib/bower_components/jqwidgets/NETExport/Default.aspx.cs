using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;
using System.Web.UI;
using System.Web.UI.WebControls;
using System.IO;
using System.Configuration;

namespace jQWidgets
{
    public partial class _Default : Page
    {
        protected void Page_Load(object sender, EventArgs e)
        {
            string gridData = Request.Form["gridData"];

            if (gridData != null)
            {
                var fileName = Request.Form["fileName"];
                File.WriteAllText(Server.MapPath(fileName + ".xls"), gridData);
            }
        }
    }
}