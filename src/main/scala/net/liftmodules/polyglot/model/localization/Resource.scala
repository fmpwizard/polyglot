package net.liftmodules.polyglot.model.localization
import scala.xml.Node
import scala.xml.Elem
import scala.collection.mutable.LinkedHashMap

class Resource() {
   
  private var name: String = null
  private var localizations = new LinkedHashMap[String, String]
  //Returns the name(description) of this resource
 
  def checkPatternExist(query: String)={
     if(name.toLowerCase().contains(query) ){
       true
     }
     else if( name.toLowerCase().startsWith(query)){
       true
     }
     else if(name.toLowerCase().endsWith(query)){
       true
     }  
     else{
       val locResult = localizations.filter(l => l._2.toLowerCase().contains(query) || l._2.toLowerCase().startsWith(query) ||l._2.toLowerCase().endsWith(query))
       if(locResult.size > 0)
         true
       else
         false
        
     }
   }
  
  /*
   * Returns the name of the resource group*/
  def getName = name
  
  /*
   * Sets the name of the resource group to the given resource name*/
  def setName(_name: String) = name = _name
  
  def  getLocalizations() =
    localizations 
    
  def  setLocalizations(newLocalizations:  LinkedHashMap[String, String]) =
    localizations = newLocalizations
        
  /*Adds a new localization of a resource to the localizations
   *  map with the key having the name as of the new resource*/
  def  addLocalization(language:String,  value:String) =
    localizations += language -> value 
        
   
  /*Deletes the localization of a resource with the given language type*/
  def deleteResource(language: String) = 
    localizations.retain((k,v) => k == language )  
    def getResourceXMLNode={
     
  }
    
  /*
   * Returns the related XML element that will be used for reproducing the localization XML file
   */
  def getLocalizationXMLElement(lang: String, loc: String)={
    val xml =
   ( <res name={getName} lang={lang} >{loc}</res> )
    
    xml
  }

}