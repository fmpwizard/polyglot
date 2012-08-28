package net.liftmodules.polyglot.model.localization

import scala.xml.Node
import scala.xml.Comment
import scala.collection.mutable.LinkedHashMap

class ResourceGroup() {
 
  private var name: String = null
  private val resources = new LinkedHashMap[String, Resource]

  
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
       val resResult = resources.filter(r => r._2.checkPatternExist(query))
        if(resResult.size > 0)
         true
       else
         false
        
     }
   }
  
  
  /*
   * Returns the name(description) of this resource group
   */
  def getName = name
  
  def setName(newName: String) = 
    name = newName
        
  def getResources =  resources
    
  /*
   * Adds a new resource to the resources map with the key having the name as of the new resource
   */
  def addResource(resource: Resource) =
   resources += resource.getName -> resource 
   
  /*
   * Deletes the resource with the given resource name
   */
  def deleteResource(resourceName: String) = 
    resources.retain((k,v) => k == resourceName )  
  
  /*
   * Returns the comment element that will be used to reproduce the localization XML file
   */
  def getComment={
    val comment = new Comment(name)
    comment
  }
 
  
  
   
   
}