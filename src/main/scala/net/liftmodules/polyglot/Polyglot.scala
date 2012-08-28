package net.liftmodules{
package polyglot {

import _root_.net.liftweb.sitemap.{Menu, SiteMap}
import net.liftweb.sitemap.Loc._
import _root_.net.liftweb.http.{LiftRules, S}
import _root_.net.liftweb.sitemap.Loc.LocGroup
import _root_.net.liftweb.http.NoticeType
import _root_.net.liftweb.common._
import net.liftweb.http.ResourceServer


object Polyglot {
  /**
  * Initializes the module. Adds its packages to the 
  * module user app's packages and allows its resources 
  * such as webpages to be accessed by the apps.
  */
  def init {
		LiftRules.addToPackages("net.liftmodules.polyglot")
		//val menu = Menu("Admin") / "admin" / "localization" / "index" >> LocGroup("admin") 
	  
		//val mutator = SiteMap.addMenusAtEndMutator(menu :: Nil)
	//  val mutator = SiteMap.addMenusAtEndMutator(sitemap.asInstanceOf)
		//LiftRules.siteMap.foreach(sitemap => LiftRules.setSiteMap(mutator(sitemap))) 
		//LiftRules.setSiteMap(SiteMap(sitemap: _*))
	  
		ResourceServer.allow({
      //case "html" :: "admin" :: admin :: Nil => true
    //  case "html" :: _ => true
     // case "admin" :: "localization" :: "index.html" ::  Nil => true
		  //case "admin" :: "localization" ::  _ => true
		 // case "admin" :: tail=> true
     case "admin" :: _ => true     
  //  LiftRules.setSiteMap(SiteMap(menus:_*))
    })
		// set the time that notices should be displayed and then fadeout
	//	LiftRules.noticesAutoFadeOut.default.set((notices: NoticeType.Value) => Full(2 seconds, 2 seconds))
	}
  
  /**
   * Defines the sitemap for the Polyglot module. 
   */
  def sitemap : List[Menu]= List(
    Menu("Admin") / "admin" / "localization" / "index" >> LocGroup("hasan"),
    Menu("View Resource") / "admin" / "localization" / "viewResource" >> LocGroup("admin")  >> Hidden,
    Menu("Edit Resource") / "admin" / "localization" / "editResource" >> LocGroup("admin")  >> Hidden,
    Menu("Delete Resource") / "admin" / "localization" / "deleteResource" >> LocGroup("admin")  >> Hidden,
    Menu("Add Resource") / "admin" / "localization" / "addResource" >> LocGroup("admin")  >> Hidden,
    Menu("Add Localization") / "admin" / "localization" / "addLocalization" >> LocGroup("admin")  >> Hidden,
    Menu("Add Resource Group") / "admin" / "localization" / "addResourceGroup" >> LocGroup("admin")  >> Hidden, 
    Menu("Edit Resource Group") / "admin" / "localization" / "editResourceGroup" >> LocGroup("admin")  >> Hidden, 
    Menu("Delete Resource Group") / "admin" / "localization" / "deleteResourceGroup" >> LocGroup("admin")  >> Hidden,
    Menu("Edit Resource Group") / "admin" / "localization" / "search" >> LocGroup("admin")  >> Hidden
    /*,
    Menu("Localization") / "admin" / "localization" / "index" >> LocGroup("admin")*/
  ) 
  
  /**
  * Insert this LocParam into your menu if you want the
  * Polyglot’s menu items to be inserted at the same level
  * and after the item
  */
  final case object AddPolyglotMenusAfter extends LocParam[Any]
  /**
  * replace the menu that has this LocParam with the Polyglot’s menu
  * items
  */
  final case object AddPolyglotMenusHere extends LocParam[Any]
  /**
  * Insert this LocParam into your menu if you want the
  * Polyglot’s menu items to be children of that menu
  */
  final case object AddPolyglotMenusUnder extends LocParam[Any]
  
  private lazy val AfterUnapply = SiteMap.buildMenuMatcher(_ == AddPolyglotMenusAfter)
  private lazy val HereUnapply = SiteMap.buildMenuMatcher(_ == AddPolyglotMenusHere)
  private lazy val UnderUnapply = SiteMap.buildMenuMatcher(_ == AddPolyglotMenusUnder)
  
  /**
   * The SiteMap mutator function
   */
  def sitemapMutator: SiteMap => SiteMap = SiteMap.sitemapMutator {
    case AfterUnapply(menu) => menu :: sitemap
    case HereUnapply(_) => sitemap
    case UnderUnapply(menu) => List(menu.rebuild(_ ::: sitemap))
  }(SiteMap.addMenusAtEndMutator(sitemap))
  
  }
}
}