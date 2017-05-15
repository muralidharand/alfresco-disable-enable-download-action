package net.codingfreaks.opensource.web.evaluator.doclib.action;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.web.evaluator.BaseEvaluator;
import org.alfresco.web.scripts.DictionaryQuery;
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

/**
 * A Document Library Action Evaluator, which decides if an action should
 *  be shown or not based on the current user's role within that site.
 *  
 * TODO Caching
 */
public class DownloadActionEvaluator extends BaseEvaluator
{
   // Logger
   private static final Log logger = LogFactory.getLog(DownloadActionEvaluator.class);
   private static final String ST_SITE = "st:site";

   private String requiredRole ="SiteManager";
   private DictionaryQuery dictionaryQuery;
   
   

   @Override
   public boolean evaluate(JSONObject jsonObject) {
      try
      {
         /* Object lockType = getProperty(jsonObject, PROP_LOCKTYPE);
            if (lockType != null && ((String) lockType).equalsIgnoreCase(NODE_LOCK))
            {
               return false;
            }
          */
         
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
   
   private String getSiteName(JSONObject jsonObject)
   {
      // Is the site supplied in the JSON directly?
      String siteName = super.getSiteId(jsonObject);
      if (siteName != null)
      {
         return siteName;
      }
      
      // See if we're processing a site node, and grab from that if so
      siteName = getNameIfSite((JSONObject)jsonObject.get("node"));
      if (siteName == null)
      {
         // Try the parent too
         siteName = getNameIfSite((JSONObject)jsonObject.get("parent"));
      }
      return siteName;
   }
   private String getNameIfSite(JSONObject node)
   {
      if (node == null) return null;
      
      // Get the site, and see if it's a site / site subtype
      String type = (String)node.get("type");
      if (type == null) return null;
      
      if (ST_SITE.equals(type) || dictionaryQuery.isSubType(type, ST_SITE))
      {
         // If it's a site, we want the cm:name property
         JSONObject props = (JSONObject)node.get("properties");
         if (props == null) return null;
         
         return (String)props.get("cm:name");
      }
      else
      {
         // This is not a site node, so we can't get a site name
         return null;
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