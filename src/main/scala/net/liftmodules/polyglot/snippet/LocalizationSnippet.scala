package net.liftmodules.polyglot.snippet

import scala.xml.NodeSeq
import net.liftweb.common.Box.box2Option
import net.liftweb.common.StringOrNodeSeq.strTo
import net.liftweb.common.Full
import net.liftweb.http.S
import net.liftweb.http.SHtml
import net.liftweb.http.StatefulSnippet
import net.liftweb.util.Helpers.strToCssBindPromoter
import net.liftmodules.polyglot.model.localization.LocalizationManager
import scala.collection.mutable.HashMap
import scala.collection.mutable.LinkedHashMap
import net.liftweb.common.Empty

/**
 * A snippet that binds behavior, functions,
 * to HTML elements
 */
class LocalizationSnippet extends StatefulSnippet {

  private var resourceGroupId: Int = 0
  private var resourceName: String = null
  private var newResourceName: String = ""
  private var localizationEn: String = ""
  private var localizations = List[String]()
  private var localizationMap = LinkedHashMap[String, String]()
  private var isResourceEdited = false
  private var newLocalizationLanguage = ""
  private var newLocalization = ""
  private var oldResourceGroupName = ""
  private var newResourceGroupName = ""
  private var searchInput = ""

  def dispatch = {
    case "deleteResourceForm" => deleteResourceForm _
    case "editResourceForm" => editResourceForm _
    case "addResourceForm" => addResourceForm _
    case "addLocalizationForm" => addLocalizationForm _
    case "addResourceGroupForm" => addResourceGroupForm _
    case "editResourceGroupForm" => editResourceGroupForm _
    case "deleteResourceGroupForm" => deleteResourceGroupForm _
    case "searchForm" => searchForm _
  }

  // associate behavior with each HTML element
  def searchForm(template: NodeSeq): NodeSeq = {
    //get the variables from the request
    def search {
      redirectTo("/admin/localization/search?query=" + searchInput)
    }
    (
      "name=searchInput" #> SHtml.text(searchInput, searchInput = _) &
      "type=submit" #> SHtml.button(("Search"), () => search)).apply(template)
  }

  // associate behavior with each HTML element
  def deleteResourceGroupForm(template: NodeSeq): NodeSeq = {
    //get the variables from the request
    resourceGroupId = S.param("resourceGroupId").get.toInt

    (

      "type=submit" #> SHtml.button(("Delete"), () => deleteResourceGroup) &
      "#cancel [onclick]" #> "window.location.href='/admin/localization/index';").apply(template)
  }

  // associate behavior with each HTML element
  def editResourceGroupForm(template: NodeSeq): NodeSeq = {
    resourceGroupId = S.param("resourceGroupId").get.toInt
    oldResourceGroupName = LocalizationManager.getResourceGroups(resourceGroupId).getName

    (
      "#cancel [onclick]" #> "window.location.href='/admin/localization/index';" &
      "type=submit" #> SHtml.button(("Save"), () => editResourceGroup) &
      "name=resourceGroupName" #> SHtml.textarea(oldResourceGroupName, newResourceGroupName = _)).apply(template)
  }

  // associate behavior with each HTML element
  def addResourceGroupForm(template: NodeSeq): NodeSeq = {
    (
      "#cancel [onclick]" #> "window.location.href='/admin/localization/index';" &
      "type=submit" #> SHtml.button(("Save"), () => addResourceGroup) &
      "name=resourceGroupName" #> SHtml.textarea("", newResourceGroupName = _)
    ).apply(template)
  }
  def getLanguage(lang : String)={ 
    val languages = Map(("en", "English"), ("de", "German") )
    languages.get(lang).get
  } 
  // associate behavior with each HTML element
  def addLocalizationForm(template: NodeSeq): NodeSeq = {
    //get the variables from the request
    resourceGroupId = S.param("resourceGroupId").get.toInt
    resourceName = S.param("resourceName").get
      
    val languages = Map(("en", "English"), ("de", "German") )
    (
      "type=submit" #> SHtml.button(("Save"), () => addLocalization) &
      //"name=language" #> SHtml.text("", newLocalizationLanguage = _) &
      "name=localization" #> SHtml.textarea("", newLocalization = _) &
      "#languages" #>   SHtml.select(languages.toSeq, Empty , newLocalizationLanguage = _)
    ).apply(template)
  }

  // associate behavior with each HTML element
  def addResourceForm(template: NodeSeq): NodeSeq = {
    //get the variables from the request
    resourceGroupId = S.param("resourceGroupId").get.toInt

    (
      "type=submit" #> SHtml.button(("Save"), () => addResource) &
      "name=resourceName" #> SHtml.text(resourceName, newResourceName = _) &
      "name=localizationEn" #> SHtml.textarea(localizationEn, localizationEn = _)).apply(template)
  }

  // associate behavior with each HTML element
  def editResourceForm(template: NodeSeq): NodeSeq = {
    //get the variables from the request
    resourceGroupId = S.param("resourceGroupId").get.toInt
    resourceName = S.param("resourceName").get
    localizationMap = LocalizationManager.getResourceGroups(resourceGroupId).getResources.get(resourceName).get.getLocalizations().clone()
    val newLocalizationLink = "/admin/localization/addLocalization?resourceGroupId=" + resourceGroupId + "&resourceName=" + resourceName
    val editResourceLink = "/admin/localization/editResource?resourceGroupId=" + resourceGroupId + "&resourceName=" + resourceName
    def addLocalizationsToMap(lang: String, loc: String) = {
      if (!(localizationMap.get(lang).get == loc)) {
        localizationMap(lang) = loc
        isResourceEdited = true
      }
    }

    (
      "#resourceGroupName" #> LocalizationManager.getResourceGroups(resourceGroupId).getName &
      "#resourceNameInput [value]" #> LocalizationManager.getResourceGroups(resourceGroupId).getResources.get(resourceName).get.getName &
      "#newLocalizationLink [href]" #> newLocalizationLink &
      "#cancel [onclick]" #> "window.location.href='/admin/localization/index';" &
      ".languageEntry *" #> LocalizationManager.getResourceGroups(resourceGroupId).getResources.get(resourceName).get.getLocalizations().map(x => ".language *" #> x._1 &
        //make the binding for multiple textarea elements
        "name=localizationEn" #> SHtml.textarea(x._2, addLocalizationsToMap(x._1, _)) &
        ".deleteLocalization" #> SHtml.button("Delete", () => deleteLocalization(x._1))) &
      "type=submit" #> SHtml.button(("Save"), () => editResource) &
      "name=resourceName" #> SHtml.text(resourceName, newResourceName = _)).apply(template)
  }

  // associate behavior with each HTML element
  def deleteResourceForm(template: NodeSeq): NodeSeq = {
    //get the variables from the request
    resourceGroupId = S.param("resourceGroupId").get.toInt
    resourceName = S.param("resourceName").get

    (
      "type=submit" #> SHtml.button(("Delete"), () => deleteResource)).apply(template)
  }

  private def deleteResourceGroup() {
    val deleteResourceGroupLink = "/admin/localization/deleteResourceGroup?resourceGroupId=" + resourceGroupId
    val defaultRedirect = "/admin/localization/index"
    var deleteResourceGroupResult = LocalizationManager.deleteResourceGroup(resourceGroupId)
    deleteResourceGroupResult match {
      case Full("successful") => {
        S.notice("Resource group is deleted successfully")
        redirectTo(defaultRedirect)
      }
      case Full("noResourceGroupError") => {
        S.error("Localization file does not contain any resource groups")
        redirectTo(defaultRedirect)
      }
      case Full("resourceGroupDoesNotExist") => {
        S.error("Resource group does not exist")
        redirectTo(defaultRedirect)
      }
      case Full("fileWriteError") => {
        S.error("Resources file could not be updated. Please try again.")
        redirectTo(deleteResourceGroupLink)
      }
      case _ => {
        redirectTo(defaultRedirect)
      }
    }
  }

  /*
   * Makes the required checks and saves the new resource group in case evertyhing is ok
   */
  private def editResourceGroup() {
    val boolean = LocalizationManager.doesResourceExist("add")

    val defaultRedirect = "/admin/localization/index"
    val editResourceGroupLink = "/admin/localization/editResourceGroup?resourceGroupId=" + resourceGroupId
    val editResourceGroupResult = LocalizationManager.setResourceGroupName(resourceGroupId, newResourceGroupName.trim())
    editResourceGroupResult match {
      case Full("successful") => {
        S.notice("Resource group name is changed successfully")
        redirectTo(defaultRedirect)
      }
      case Full("resourceGroupWithSameNameExists") => {
        S.error("Adding new resource group was not successful. A resource group with the  name  : " + newResourceGroupName + " already exists")
        redirectTo(editResourceGroupLink)
      }
      case Full("noResourceGroupError") => {
        S.error("Localization file does not contain any resource groups")
        redirectTo(defaultRedirect)
      }

      case Full("resourceGroupDoesNotExist") => {
        S.error("Resource group does not exist")
        redirectTo(defaultRedirect)
      }
      case Full("currentResourceGroupNameIsTheSameAsNew") => {
        redirectTo(defaultRedirect)
      }

      case Full("fileWriteError") => {
        S.error("Resources file could not be updated. Please try again.")
        redirectTo(editResourceGroupLink)
      }

      case _ => {
        redirectTo(defaultRedirect)
      }

    }

  }

  def deleteLocalization(locLang: String) {
    val editResourceLink = "/admin/localization/editResource?resourceGroupId=" + resourceGroupId + "&resourceName=" + resourceName
    val defaultRedirect = "/admin/localization/index"
    val localizationDeleteResult = LocalizationManager.deleteLocalization(resourceGroupId, resourceName, locLang)
    localizationDeleteResult match {
      case Full("successful") => {
        S.notice("Localization deleted successfully")
        redirectTo(editResourceLink)
      }
      case Full("noResourceGroupError") => {
        S.error("Localization file does not contain any resource groups")
        redirectTo(defaultRedirect)
      }

      case Full("resourceGroupDoesNotExist") => {
        S.error("Resource group does not exist")
        redirectTo(defaultRedirect)
      }

      case Full("resourceDoesNotExist") => {
        S.error("Resource does not exist")
        redirectTo(defaultRedirect)
      }

      case Full("localizationDoesNotExist") => {
        S.error("Localization does not exist")
        redirectTo(defaultRedirect)
      }
      case Full("fileWriteError") => {
        S.error("Resources file could not be updated. Please try again.")
        redirectTo(editResourceLink)
      }
      case _ => {
        redirectTo(editResourceLink)
      }

    }

  }

  /**
   * Makes the required checks and saves the new resource group in case evertyhing is ok
   */
  private def addResourceGroup() {
    val defaultRedirect = "/admin/localization/index"
    val addResourceGroupLink = "/admin/localization/addResourceGroup"
    val addResourceGroupResult = LocalizationManager.addResourceGroup(newResourceGroupName)
    addResourceGroupResult match {
      case Full("successful") => {
        S.notice("Resource group is added successfully")
        redirectTo(defaultRedirect)
      }
      case Full("resourceGroupWithSameNameExists") => {
        S.error("Adding new resource group was not successful. A resource group with the  name  : " + newResourceGroupName + " exists")
        redirectTo(addResourceGroupLink)
      }

      case Full("fileWriteError") => {
        S.error("Resources file could not be updated. Please try again.")
        redirectTo(addResourceGroupLink)
      }
      case _ => {
        redirectTo(defaultRedirect)
      }
    }
  }

  /**
   * Makes the required checks and saves the new localization in case evertyhing is ok
   */
  private def addLocalization() {
    val addLocalizationLink = "/admin/localization/addLocalization?resourceGroupId=" + resourceGroupId + "&resourceName=" + resourceName
    val editResourceLink = "/admin/localization/editResource?resourceGroupId=" + resourceGroupId + "&resourceName=" + resourceName

    val addLocalizationResult = LocalizationManager.addLocalization(resourceGroupId, resourceName, newLocalizationLanguage, newLocalization)
    addLocalizationResult match {
      case Full("successful") => {
        S.notice("Localization is added successfully")
        redirectTo(editResourceLink)
      }
      case Full("localizationForLanguageExists") => {
        S.error("Adding new localization was not successful. A localization for "+ getLanguage(newLocalizationLanguage)  + " already exists")
        redirectTo(addLocalizationLink)
      }

      case Full("fileWriteError") => {
        S.error("Resources file could not be updated. Please try again.")
        redirectTo(editResourceLink)
      }
      case _ => {
        redirectTo(editResourceLink)
      }
    }

  }

  /**
   * Makes the required checks and saves the new resource in case evertyhing is ok
   */
  private def addResource() {
    val defaultRedirect = "/admin/localization/index"
    val addResourceLink = "/admin/localization/addResource?resourceGroupId=" + resourceGroupId
    val addResourceResult = LocalizationManager.addResource(resourceGroupId, newResourceName.trim(), localizationEn)
    addResourceResult match {
      case Full("successful") => {
        S.notice("Resource is added successfully")
        redirectTo(defaultRedirect)
      }
      case Full("resourceWithSameNameExist") => {
        S.error("Adding new resource was not successful. A resource with the same name already exists")
        redirectTo(addResourceLink)
      }

      case Full("fileWriteError") => {
        S.error("Resources file could not be updated. Please try again.")
        redirectTo(addResourceLink)
      }
      case _ => {
        redirectTo(defaultRedirect)
      }
    }
  }

  private def editResource() {
    val defaultRedirect = "/admin/localization/index"
    val editResourceLink = "/admin/localization/editResource?resourceGroupId=" + resourceGroupId + "&resourceName=" + resourceName
    //in case there is no change in the name, just redirect to the localization page

    //in case user did not do any changes, just redirect to where we came from 
    if ((resourceName == newResourceName) && !isResourceEdited) {
      redirectTo(defaultRedirect)
    }
    val editResult = LocalizationManager.updateResource(resourceGroupId, resourceName, newResourceName.trim(), localizationMap, isResourceEdited)
    editResult match {
      case Full("successful") => {
        S.error("Resource edited")
        redirectTo(defaultRedirect)
      }

      case Full("noResourceGroupError") => {
        S.error("Localization file does not contain any resource groups")
        redirectTo(defaultRedirect)
      }

      case Full("resourceGroupDoesNotExist") => {
        S.error("Resource group does not exist")
        redirectTo(defaultRedirect)
      }
      case Full("resourceWithSameNameExist") => {
        S.error("Updating resource was not successful. A resource with the same name already exists")
        redirectTo(editResourceLink)
      }

      case Full("resourceDoesNotExist") => {
        S.error("Resource does not exist")
        redirectTo(defaultRedirect)
      }
      case Full("currentResourceNameIsSameAsNew") => {
        redirectTo(defaultRedirect)
      }

      case Full("fileWriteError") => {
        S.error("Resources file could not be updated. Please try again.")
        redirectTo(editResourceLink)
      }

      case _ => {
        redirectTo(defaultRedirect)
      }

    }

  }

  private def deleteResource() {
    val deleteResourceLink = "/admin/localization/deleteResource?resourceGroupId=" + resourceGroupId + "&resourceName=" + resourceName
    val defaultRedirect = "/admin/localization/index"
    var deleteResult = LocalizationManager.deleteResource(resourceGroupId, resourceName)
    deleteResult match {
      case Full("successful") => {
        S.notice("Resource is deleted successfully")
        redirectTo(defaultRedirect)
      }

      case Full("noResourceGroupError") => {
        S.notice("Localization file does not contain any resource groups")
        redirectTo(defaultRedirect)
      }

      case Full("resourceGroupDoesNotExist") => {
        S.notice("Resource group does not exist")
        redirectTo(defaultRedirect)
      }

      case Full("resourceDoesNotExist") => {
        S.notice("Resource does not exist")
        redirectTo(defaultRedirect)
      }

      case Full("fileWriteError") => {
        S.error("Resources file could not be updated. Please try again.")
        redirectTo(defaultRedirect)
      }
      case _ => {
        redirectTo(defaultRedirect)
      }
    }
  }

}