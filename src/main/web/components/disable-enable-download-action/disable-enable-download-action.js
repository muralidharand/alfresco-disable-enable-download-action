(function () {
    var me = this;

    YAHOO.Bubbling.fire("registerAction", {
        actionName: "onDisableDownload",
        fn: function dlA_onDisableDownload(asset) {

             
            var node  = new Alfresco.util.NodeRef(asset.nodeRef).uri;
            var url = Alfresco.constants.PROXY_URI + "slingshot/doclib/action/aspects/node/" + node;
            Alfresco.util.Ajax.jsonRequest({
                url: url,
                method: "POST",
                dataObj:
                {
                 added: ["cf:disableDownloadAspect"]
                },
                successCallback: 
                {
                 fn: function(response)
                    {
                        Alfresco.util.PopupManager.displayMessage(
                        {
                           text: "Download action disabled successfully"
                        });
                        window.location.reload();
                    }
                },
                
                failureCallback:
                {
                fn: function(response)
                   {
                       this.msg("message.failure")
                   }
                }
            });
        }
    });
    YAHOO.Bubbling.fire("registerAction", {
        actionName: "onEnableDownload",
        fn: function dlA_onEnableDownload(asset) {
            
         
            var node  = new Alfresco.util.NodeRef(asset.nodeRef).uri;
            var url = Alfresco.constants.PROXY_URI + "slingshot/doclib/action/aspects/node/" + node;
            Alfresco.util.Ajax.jsonRequest({
                url: url,
                method: "POST",
                dataObj:
                {
                 removed: ["cf:disableDownloadAspect"]
                },
                successCallback: 
                {
                 fn: function(response)
                    {
                        Alfresco.util.PopupManager.displayMessage(
                        {
                           text: "Download action enabled successfully"
                        });
                        window.location.reload();
                    }
                },
                
                failureCallback:
                {
                fn: function(response)
                   {
                       this.msg("message.failure")
                   }
                }
            });
        }
    });

 

})();