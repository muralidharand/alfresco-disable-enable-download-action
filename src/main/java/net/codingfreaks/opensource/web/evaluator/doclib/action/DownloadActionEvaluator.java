package net.codingfreaks.opensource.web.evaluator.doclib.action;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.web.evaluator.BaseEvaluator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.extensions.surf.RequestContext;
import org.springframework.extensions.surf.ServletUtil;
import org.springframework.extensions.surf.support.ThreadLocalRequestContext;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.connector.Connector;
import org.springframework.extensions.webscripts.connector.Response;

public class DownloadActionEvaluator extends BaseEvaluator
{
   // Logger
   private static final Log logger = LogFactory.getLog(DownloadActionEvaluator.class);
   private String requiredRole ="SiteManager";
  

   @Override
   public boolean evaluate(JSONObject jsonObject) {
      try
      {
         String username = getUserId();
         JSONObject modifier =(JSONObject) getProperty(jsonObject,"cm:modifier");
         
         if (username.equalsIgnoreCase((String)modifier.get("userName")))
         {
             return true;
         }
         
         
         
         String siteName = getSiteId(jsonObject);
         
         if (siteName == null)
         {
            // It's not a site, so we have no opinion on access
            return true;
         }

         // Fetch the membership information for the site
         RequestContext rc = ThreadLocalRequestContext.getRequestContext();
         Connector conn = rc.getServiceRegistry().getConnectorService().getConnector(
               "alfresco", username, ServletUtil.getSession());
         Response response = conn.call("/api/sites/"+siteName+"/memberships/"+username);
         
         if (response.getStatus().getCode() == Status.STATUS_OK)
         {
            // Convert the response text to jsonobject
            JSONObject responsetext = (JSONObject)new JSONParser().parse(response.getResponse());
            
            // Get the user role and compare with required role
            return requiredRole.equalsIgnoreCase(this.getUserRole(responsetext));         
         }
         else if (response.getStatus().getCode() == Status.STATUS_NOT_FOUND)
         {
            // Not a member of the site / site not found / etc
            // Shouldn't be showing in this case
            return false;
         }
         else
         {
            logger.warn("Invalid response fetching memberships for " + username + " in " + siteName + " - " + response);
            return false;
         }
      }
      catch (Exception err)
      {
         throw new AlfrescoRuntimeException("Failed to run UI evaluator: " + err.getMessage());
      }
    }
     
   private String getUserRole(JSONObject jsonObject)
   {
       String rolename = null;

       try
       {
          rolename = (String) jsonObject.get("role");

          if (rolename.isEmpty())
          {
             throw new AlfrescoRuntimeException("Unable to get the user role");
          }
       }
       catch (Exception err)
       {
           throw new AlfrescoRuntimeException("Exception whilst querying siteId from location: " + err.getMessage());
       }

       return rolename;
   }

}