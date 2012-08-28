package net.liftmodules.polyglot.model.localization
import scala.xml.XML

class Localization {
	val localization =
			<resources>
		{for (resGroup <- LocalizationManager.getResourceGroups) yield{
			{resGroup.getComment}
			for (resMap <- resGroup.getResources) yield {

				for(loc <- resMap._2.getLocalizations()) yield {
					{resGroup.getComment}
					<res></res>
				}

			}
		}
		}
	</resources>

	def printXML = println("resulting XML : \n"+localization)
	
	def write = XML.saveFull(".\\src\\main\\resources\\_resources.html.orig2", localization, "UTF-8", true, null) 
}