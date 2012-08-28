package net.liftmodules.polyglot.model.localization
import scala.xml.Comment
import net.liftweb.common.Box.box2Iterable
import net.liftweb.common.Box
import net.liftweb.common.Full
import net.liftweb.http.LiftRulesMocker.toLiftRules
import net.liftweb.http.LiftRules
import scala.xml.Node
import scala.collection.mutable.HashMap
import scala.xml.Elem
import scala.xml.XML
import java.nio.charset.UnmappableCharacterException
import java.io.FileNotFoundException
import scala.collection.mutable.LinkedHashMap
import java.io.FileWriter
import java.io.OutputStreamWriter
import java.io.FileOutputStream

object LocalizationManager {
//  private val localizationFilePath = "/_resources.html.orig" 
  private val localizationFilePath = "./src/main/webapp/_resources.html.orig"//"./src/main/webapp/_resources.html.orig"
  private var resourceGroups = List[ResourceGroup]()
  //A flag to indicate whether or not the localization file is already loaded in the memory
  private var isLoaded: Boolean = false
  private var localization = new Localization()
   
  
  /*def buildXML() : Node={
    
    def addChild(n: Node, newChild: Node) = n match {
	  case Elem(prefix, label, attribs, scope, child @ _*) =>
	    Elem(prefix, label, attribs, scope, child ++ newChild : _*)
	  case _ => error("Can only add children to elements!")
    } 

    var xml : Node =
      <resources> 
      </resources>
      
    for(resGroup <- resourceGroups){
      xml = addChild(xml, resGroup.getComment)
      for(res <- resGroup.getResources){
        for(loc <- res._2.getLocalizations()){
          xml = addChild(xml,res._2.getLocalizationXMLElement(loc._1, loc._2))
        }    
      }
    }
    xml
  
  }*/
  
  /*def writeToResourcesFile2(filePath : String, xmlNode: Node)  : Boolean = {
    try{
       XML.save(filePath, xmlNode, "utf-8", true, null)
       ResourceFileBuilder.write
       true
    }catch{
      case e: UnmappableCharacterException =>{
        e.printStackTrace()
        reloadLocalizationXML
         false
      } 
      case e :FileNotFoundException => {
        reloadLocalizationXML
        false 
      }
     } 
  }*/
  
   private def writeToResourcesFile(filePath : String)  : Boolean = {
    try{
      val resourcesFile = new OutputStreamWriter(new FileOutputStream(filePath),"UTF-8");
      
      resourcesFile.write("<?xml version='1.0' encoding='utf-8'?>\n")
      resourcesFile.write("<resources>\n")
      
      for(resGroup <- resourceGroups){
        resourcesFile.write("  <!-- " +resGroup.getName+ " -->\n\n")
        for(res <- resGroup.getResources){
          for(loc <- res._2.getLocalizations()){
          	resourcesFile.write("  <res name=\"" + res._2.getName + "\" lang=\"" + loc._1 + "\">" + loc._2 + "</res>\n")
          }
          resourcesFile.write("\n")
        }
        resourcesFile.write("\n")
      }
      
      resourcesFile.write("</resources>")
      resourcesFile.close()
      true
    }catch{
      case e: UnmappableCharacterException =>{
        reloadLocalizationXML
         false
      } 
      case e :FileNotFoundException => {
        reloadLocalizationXML
        false 
      }
      case _ => {
        reloadLocalizationXML
        false 
      }
     } 
  }
  
  def updateResourcesXMLFile() = {
  
     val localizationWritePath = "./src/main/webapp/_resources.html.orig"
     val isSuccessful = writeToResourcesFile(localizationWritePath)
     
     if(isSuccessful)
       true
     else
       false
  }

  def setResourceGroupName(resourceGroupId : Int, newResourceGroupName : String) : Box[String]={
    var isExists = false
    val newResourceGroupNameTrimmed = newResourceGroupName.trim()
    
    if( resourceGroups.size  == 0 )
      return new Full("noResourceGroupError")
    else if((resourceGroupId < 0) || ((resourceGroupId > resourceGroups.size - 1)) ){
      println("bounds ")
      return new Full("resourceGroupDoesNotExist") 
     }
   
    val resourceGroup = getResourceGroups(resourceGroupId)
    if(resourceGroup.getName ==  newResourceGroupNameTrimmed)
      return new Full("currentResourceGroupNameIsTheSameAsNew") 
    
   
    for(resGroup <- resourceGroups){    
      if((resGroup.getName) == (newResourceGroupNameTrimmed)){
         isExists = true
         return new Full("resourceGroupWithSameNameExists") 
      } 
    } 
   
    resourceGroup.setName(newResourceGroupNameTrimmed)
    val isUpdated = updateResourcesXMLFile
    if(isUpdated){
      return new Full("successful")
    }
    else{
      return new Full("fileWriteError")
    }
   
  }
  
  /**
   * Removes the element of a given list at given index and returns a new lust
   */
  private def remove[T](list: List[T], index : Int) = {
	  val (start, _ :: end) = list.splitAt(index)
	  start ::: end
  }
  
  /**
   * Deletes an element from the resource groups at the given index
   */
  def deleteResourceGroup( resourceGroupId: Int) :Box[String] = {   
    if( resourceGroups.size  == 0 )
      return new Full("noResourceGroupError")
    else if((resourceGroupId < 0) || ((resourceGroupId > resourceGroups.size - 1)) )
      return new Full("resourceGroupDoesNotExist")

    else{
      resourceGroups = remove(resourceGroups,resourceGroupId)
	  //TODO :  Write into the file
      val isUpdated = updateResourcesXMLFile
      if(isUpdated){
        return new Full("successful")
      }
      else{
        return new Full("fileWriteError")
      }  
    } 
  }
  
  /**
   * adds a new resource group to the model
   * */
  def addResourceGroup(resourceGroupName : String) : Box[String]={
    val resourceGroupNameTrimmed = resourceGroupName.trim()
    var isExists = false
    for(resGroup <- resourceGroups){    
      if((resGroup.getName) == (resourceGroupNameTrimmed)){
         isExists = true
         return new Full("resourceGroupWithSameNameExists") 
      }
        
    }  
   
    val newResourceGroup = new ResourceGroup()
	newResourceGroup.setName(resourceGroupNameTrimmed)
    resourceGroups ::= newResourceGroup
    val isUpdated = updateResourcesXMLFile
    if(isUpdated){
      return new Full("successful")
    }
    else{
      return new Full("fileWriteError")
    }      
  }
  
  
  def deleteLocalization( resourceGroupId: Int, resourceName: String, locLanguage: String) :Box[String]  = {
    if( resourceGroups.size  == 0 ){
      return new Full("noResourceGroupError")
    }
       
    else if((resourceGroupId < 0) || ((resourceGroupId > resourceGroups.size - 1)) ){
       return new Full("resourceGroupDoesNotExist")
    }
     
    else if(resourceGroups(resourceGroupId).getResources.get(resourceName).isEmpty){
       return new Full("resourceDoesNotExist")
      
    }
    else if(resourceGroups(resourceGroupId).getResources.get(resourceName).get.getLocalizations().get(locLanguage).isEmpty ){
       return new Full("localizationDoesNotExist")    
    }
    else{
       //Set the name
      resourceGroups(resourceGroupId).getResources.get(resourceName).get.getLocalizations().remove(locLanguage)   
      val isUpdated = updateResourcesXMLFile
      if(isUpdated){
        return new Full("successful")
      }
      else{
        return new Full("fileWriteError")
      }
    }

  }
  
  def addLocalization(resourceGroupId: Int, resourceName: String, localizationLanguage : String, localization: String) : Box[String]={
    val langTrimmed = localizationLanguage.trim()
    val resNameTrimmed = resourceName.trim()
    val locTrimmed = localization.trim()
    
    if(doesLocalizationForResourceExist(resourceGroupId, resNameTrimmed, langTrimmed)){
      return new Full("localizationForLanguageExists") 
    }
    else{
	    val resource = resourceGroups(resourceGroupId).getResources.get(resNameTrimmed).get
	    resource.addLocalization(langTrimmed, locTrimmed)	
	    val isUpdated = updateResourcesXMLFile
        if(isUpdated){
          return new Full("successful")
        }
        else{
          return new Full("fileWriteError")
        }
    }
  }
  
  def addResource(resourceGroupId: Int, resourceName: String, localizationEn : String) : Box[String]={
    val resNameTrimmed = resourceName.trim()
    val locTrimmed = localizationEn.trim()
    if(doesResourceExist(resNameTrimmed)){
        return new Full("resourceWithSameNameExist")
    }
    else{
	    val resource = new Resource
	    resource.setName(resNameTrimmed)
	    resource.addLocalization("en", locTrimmed)
	    resourceGroups(resourceGroupId).addResource(resource)
	    val isUpdated = updateResourcesXMLFile
        if(isUpdated){
          return new Full("successful")
        }
        else{
          return new Full("fileWriteError")
        }
    }
  }
  /**
  * checks whether a resource with a given name exist
  * */  
  def doesResourceExist(name: String) : Boolean = {
    for(resGroup <-resourceGroups){
      if(!resGroup.getResources.get(name).isEmpty){
        return true
      }
    }
    return false
  }
  
  /**
   * checks whether a localization with exist for a resource on a given resource group
   * */  
  def doesLocalizationForResourceExist(resourceGroupId: Int, resourceName : String, lang: String) : Boolean = {
   
      if(resourceGroups(resourceGroupId).getResources.get(resourceName).get.getLocalizations().get(lang).isEmpty == false){
        return true
      }
    return false
  }
  
  /**
   * updates an existing resource. Updating could be changing the name of the resource,
   * adding new localizations, deleting localizations, changing localizations
   * */
  def updateResource( resourceGroupId: Int, resourceName: String, newName:String, localizationMap : LinkedHashMap[String, String], isResourceEdited: Boolean) :Box[String] = {
    val resNameTrimmed = resourceName.trim()
    val newNameTrimmed = newName.trim()
    
    if( resourceGroups.size  == 0 )
      return new Full("noResourceGroupError")
    else if((resourceGroupId < 0) || ((resourceGroupId > resourceGroups.size - 1)) )
      return new Full("resourceGroupDoesNotExist")
    else if(resourceGroups(resourceGroupId).getResources.get(resNameTrimmed).isEmpty  == true)
      return new Full("resourceDoesNotExist")
    /*there is no change*/
    else if(resNameTrimmed == newNameTrimmed && !isResourceEdited){
       return new Full("currentResourceNameIsSameAsNew")
    }
    /*Only the resource name has changed*/
    else if(resNameTrimmed == newNameTrimmed && isResourceEdited){
      val resource = resourceGroups(resourceGroupId).getResources.get(resNameTrimmed).get 
      resourceGroups(resourceGroupId).getResources.remove(resNameTrimmed)
      resource.setName(newNameTrimmed)
      resourceGroups(resourceGroupId).getResources.update(newNameTrimmed, resource) 
         
      val isUpdated = updateResourcesXMLFile
      if(isUpdated){
        return new Full("successful")
      }
      else{
        return new Full("fileWriteError")
      } 
   }
   else if(doesResourceExist(newNameTrimmed)  ){
     return new Full("resourceWithSameNameExist")
   }
    /*Both resource name and localizations has changed*/
   else{
     val resource = resourceGroups(resourceGroupId).getResources.get(resNameTrimmed).get 
      resourceGroups(resourceGroupId).getResources.remove(resNameTrimmed)
      resource.setName(newNameTrimmed)

      resourceGroups(resourceGroupId).getResources.update(newNameTrimmed, resource) 
         
      val isUpdated = updateResourcesXMLFile
	  if(isUpdated){
        return new Full("successful")
      }
      else{
        return new Full("fileWriteError")
      } 
   } 
  }
  
  /**
   * deletes a given resource from the model
   * */
  def deleteResource( resourceGroupId: Int, resourceName: String) :Box[String]  = {
     if( resourceGroups.size  == 0 ){
       return new Full("noResourceGroupError")
     }
    
    
    else if((resourceGroupId < 0) || ((resourceGroupId > resourceGroups.size - 1)) ){
       return new Full("resourceGroupDoesNotExist")
    }
     
    
    else if(resourceGroups(resourceGroupId).getResources.get(resourceName).isEmpty  == true){
      println(resourceGroupId+ " asd "+resourceName)
       return new Full("resourceDoesNotExist")
    }
    else{
       //Set the name
      resourceGroups(resourceGroupId).getResources.remove(resourceName)   
       val isUpdated = updateResourcesXMLFile
	   if(isUpdated){
         return new Full("successful")
       }
       else{
         return new Full("fileWriteError")
       } 
    }
     
     
  }

  /**
   * returns the loaded resource groups
   * */
  def getResourceGroups ={
    //If the localization file is not loaded into the memory, load it 
    if(!isLoaded)
      loadLocalizationXML
      
    //return the resource groups that are in the memory
    resourceGroups
  }
  
  /**
   * returns the loaded resource groups
   * */
  def getResources(resourceGroup: ResourceGroup) ={
    //In case the localization file is not loaded into the memory, load it 
    if(!isLoaded)
      loadLocalizationXML
      
      //return the resource groups that are in the memory
    if(resourceGroup!=null){
      resourceGroup.getResources
    }  
  } 
  
 /**
  * returns localizations of a particular resource in particular resource group 
  **/
  def getLocalizations(resourceGroupId: Int , resourceName: String)  ={
    //In case the localization file is not loaded into the memory, load it 
    if(!isLoaded)
      loadLocalizationXML
      
      //return the resource groups that are in the memory
    val  resourceGroup = resourceGroups(resourceGroupId)
    if(resourceGroup !=null){
      val resource = resourceGroups(resourceGroupId).getResources.get(resourceName).get
          
       if( resource!= null){
          resource.getLocalizations()
       }
          
     }
     else
       println("ERROR LOG: A resource group with id "+resourceGroupId+"does not exist!")
               
  } 
  
  /**
   * initiates a new resource group list and load the localization XML file to the model
   */
  def reloadLocalizationXML= {
    resourceGroups =  List[ResourceGroup]()
    loadLocalizationXML
  }

  /**
   * loads the localization XML file into the memory with the data model
   */
  def loadLocalizationXML = {
    /*LiftRules.loadResourceAsXml() uses some kind of a cache which results in getting always the first loaded 
     * version of a file when reloaded. Instead ConstructingParser is used to load the XML file*/
    val file = new java.io.File(localizationFilePath)
    val src = scala.io.Source.fromFile(file, "utf-8")
    var nodeBox = scala.xml.parsing.ConstructingParser.fromSource(src, true).document().docElem //XML.load(localizationFilePath)//LiftRules.loadResourceAsXml(localizationFilePath).openTheBox
    
    for (node <- (nodeBox.toSeq) \\ "resources") {

      val childs = node.child
      var resourceGroup: ResourceGroup = null
      var isLocalization = false
      var resource: Resource = new Resource();
      var previousResourceName = ""

      for (myChild <- childs) {
        if ((myChild).isInstanceOf[Comment]) {
          val comment = myChild.asInstanceOf[Comment]

          resourceGroup = new ResourceGroup()
          resourceGroup.setName(comment.commentText.trim())
          resourceGroups ::= resourceGroup
          isLocalization = false
        }
        if (myChild.label.equals("res")) {
          if (previousResourceName != (myChild \ "@name").text) {
            previousResourceName = (myChild \ "@name").text

            resource = new Resource()
            resource.setName((myChild \ "@name").text.trim())
            isLocalization = true
            resourceGroup.addResource(resource)
          }
          resource.addLocalization((myChild \ "@lang").text, myChild.text.trim())
        }
    	 

      }

      resourceGroups = resourceGroups

    }
    //set the flag to indicate that the file is loaded into the memory
    isLoaded = true
  }

}