package net.liftmodules.polyglot.snippet

import scala.collection.mutable.HashMap
import scala.xml.NodeSeq
import net.liftmodules.polyglot.model.localization.LocalizationManager
import net.liftweb.http.S
import net.liftweb.util.Helpers._
import net.liftweb.util._
import net.liftweb.util.Props
import scala.io.Codec
import scala.xml.XML
import net.liftmodules.polyglot.model.localization.ResourceGroup

class Localizations() {

  var resourceGroupId = -1
  //defines how many elements want to get currentResourceGroup id when resource groups are accessed

  //Initialize main localization page
  def searchResults =
    {
      val query = S.param("query").get.toLowerCase()
      var resResultList = List[ResourceGroup]()
      resResultList = LocalizationManager.getResourceGroups.filter(rg => rg.checkPatternExist(query))
      val resultString = resResultList.size.toString() + " results found"

      "#resultLabel" #> resultString &
        "#localizationsLink [href]" #> "/admin/localization/index" &
        ".resourceGroup *" #> resResultList.map(resGroup =>
          ".resourceGroupName *" #> resGroup.getName &
            ".deleteResourceGroupLink [href]" #> ("/admin/localization/deleteResourceGroup?resourceGroupId=" + incrementCurrentResourceGroupId.toString()) &
            ".editResourceGroupLink [href]" #> ("/admin/localization/editResourceGroup?resourceGroupId=" + resourceGroupId.toString()) &
            ".newResourceLink [href]" #> ("/admin/localization/addResource?resourceGroupId=" + resourceGroupId.toString()) &
            ".resource *" #> resGroup.getResources.map(x => ".localizationName *" #> x._2.getName &
              ".editLink [href]" #> ("/admin/localization/editResource?resourceGroupId=" + resourceGroupId + "&resourceName=" +
                x._2.getName) &
                ".deleteLink [href]" #> ("/admin/localization/deleteResource?resourceGroupId=" + resourceGroupId + "&resourceName=" +
                  x._2.getName) &
                  ".localizationEntry *" #> x._2.getLocalizations().map(a =>
                    ".language *" #> a._1 &
                      ".localization *" #> a._2)
            )

        ) & ".newResourceGroupLink [href]" #> ("/admin/localization/addResourceGroup")

    }

  //Initialize main localization page
  def viewResourceGroups =
    {

      ".resourceGroup *" #> LocalizationManager.getResourceGroups.map(resGroup =>
          ".resourceGroupName *" #> resGroup.getName &
          ".deleteResourceGroupLink [href]" #> ("/admin/localization/deleteResourceGroup?resourceGroupId=" + incrementCurrentResourceGroupId.toString()) &
          ".editResourceGroupLink [href]" #> ("/admin/localization/editResourceGroup?resourceGroupId=" + resourceGroupId.toString()) &
          ".newResourceLink [href]" #> ("/admin/localization/addResource?resourceGroupId=" + resourceGroupId.toString()) &
          ".resource *" #> resGroup.getResources.map(x => ".localizationName *" #> x._2.getName &
            ".editLink [href]" #> ("/admin/localization/editResource?resourceGroupId=" + resourceGroupId + "&resourceName=" +
              x._2.getName) &
              ".deleteLink [href]" #> ("/admin/localization/deleteResource?resourceGroupId=" + resourceGroupId + "&resourceName=" +
                x._2.getName) &
                "#newLocalizationLink [href]" #> ("/admin/localization/addLocalization?resourceGroupId=" + resourceGroupId + "&resourceName=" +  x._2.getName )&
                ".localizationEntry *" #> x._2.getLocalizations().map(a =>
                  ".language *" #> a._1 &
                    ".localization *" #> a._2)
          )

      ) & ".newResourceGroupLink [href]" #> ("/admin/localization/addResourceGroup")

    }

  //Used for a workaround to calculate resourceGroupId
  private def incrementCurrentResourceGroupId = {
    resourceGroupId += 1
    resourceGroupId
  }

  //Initialize add resource page
  def addResource = {

    val resourceGroupId = S.param("resourceGroupId").get.toInt

    //  "#resourceNameInput [value]" #> LocalizationManager.getResourceGroups(resourceGroupId).getResources.get(resourceName).get.getName&
    "#cancel [onclick]" #> "window.location.href='/admin/localization/index';" &
    "#resourceGroupName" #> LocalizationManager.getResourceGroups(resourceGroupId).getName
    // ".languageEntry *" #>   LocalizationManager.getResourceGroups(resourceGroupId).getResources.get(resourceName).get.getLocalizations().map(x => ".language *" #> x._1&
    //".localization *[value]" #> x._2
    //)  
  }

  //Initialize add resource page
  def addLocalization = {

    val resourceGroupId = S.param("resourceGroupId").get.toInt
    val resourceName = S.param("resourceName").get

    val cancelLink = "window.location.href='/admin/localization/editResource?resourceGroupId=" + resourceGroupId + "&resourceName=" + resourceName + "';"

    //  "#resourceNameInput [value]" #> LocalizationManager.getResourceGroups(resourceGroupId).getResources.get(resourceName).get.getName&
    "#cancel [onclick]" #> cancelLink &
      "#resourceGroupName" #> LocalizationManager.getResourceGroups(resourceGroupId).getName &
      "#resourceName" #> resourceName
    // ".languageEntry *" #>   LocalizationManager.getResourceGroups(resourceGroupId).getResources.get(resourceName).get.getLocalizations().map(x => ".language *" #> x._1&
    //".localization *[value]" #> x._2
    //)  
  }
  /*
    //Initialize edit resource page
    def editResource   = {

	     val resourceGroupId = S.param("resourceGroupId").get.toInt
	     val resourceName = S.param("resourceName").get
	     val newLocalizationLink = "/admin/localization/addLocalization?resourceGroupId="+resourceGroupId+"&resourceName="+resourceName

	     "#resourceNameInput [value]" #> LocalizationManager.getResourceGroups(resourceGroupId).getResources.get(resourceName).get.getName&
	     "#newLocalizationLink [href]" #> newLocalizationLink&
	     "#cancel [onclick]" #> "window.location.href='/admin/localization';"&
		     ".languageEntry *" #>   LocalizationManager.getResourceGroups(resourceGroupId).getResources.get(resourceName).get.getLocalizations().map(x => ".language *" #> x._1&
		     ".languageValueColumn *" #> x._2
		      ) 

    }*/

  //Initialize delete resource page
  def deleteResourceGroup = {
    val resourceGroupId = S.param("resourceGroupId").get.toInt
    val resGroup = LocalizationManager.getResourceGroups(resourceGroupId)

    ".resourceGroupName *" #> resGroup.getName &
      ".editResourceGroupLink [href]" #> ("/admin/localization/editResourceGroup?resourceGroupId=" + incrementCurrentResourceGroupId.toString()) &
      ".newResourceLink [href]" #> ("/admin/localization/addResource?resourceGroupId=" + resourceGroupId.toString()) &
      ".resource *" #> resGroup.getResources.map(x => 
        ".localizationName *" #> x._2.getName &
        ".localizationEntry *" #> x._2.getLocalizations().map(a =>
          ".language *" #> a._1 &
          ".localization *" #> a._2)            ) &
      ".newResourceGroupLink [href]" #> ("/admin/localization/addResourceGroup")
  }
  
  //Initialize delete resource page
  def deleteResource = {

    val resourceGroupId = S.param("resourceGroupId").get.toInt
    val resourceName = S.param("resourceName").get
    val resGroup = LocalizationManager.getResourceGroups(resourceGroupId)
    
    "#resourceGroupName *" #> resGroup.getName &
    "#resourceName" #> LocalizationManager.getResourceGroups(resourceGroupId).getResources.get(resourceName).get.getName &
    "#cancel [onclick]" #> "window.location.href='/admin/localization/index';" &
    ".localizationEntry *" #> LocalizationManager.getResourceGroups(resourceGroupId).getResources.get(resourceName).get.getLocalizations().map(x => 
      "#language *" #> x._1 &
      "#localization *" #> x._2
      )
  }

  /*
     def viewResource = 
     {   
         val resourceGroupId = S.param("resourceGroupId").get.toInt
	     val resourceName = S.param("resourceName").get

	     ".resourceName *" #> LocalizationManager.getResourceGroups(resourceGroupId).getResources.get(resourceName).get.getName&
	    "#backToResourceGroup [onclick]" #> "window.location.href='/admin/localization';"&
	     ".languageEntry *" #>   LocalizationManager.getResourceGroups(resourceGroupId).getResources.get(resourceName).get.getLocalizations().map(x => ".language *" #> x._1&
	         ".languageValueColumn *" #> x._2
	     ) 

    // ".resource *" #>   resourceGroups(0).getResources.map(x => ".localization *" #> x._2.getLocalizations().map(a => a._2 ))
    }*/

}

