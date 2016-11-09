import org.xml.sax.Attributes
import org.xml.sax.helpers.DefaultHandler
import java.io.File
import java.util.*
import javax.xml.parsers.SAXParserFactory

/**
 * Extract body part CUI from CPT code description
 */
val PART_CPT_MAP = HashMap<String, MutableSet<String>>()
val CPT_CUI_MAP = HashMap<String, MutableSet<CuiDesc>>()

data class CuiDesc(val cui:String, val bodyPart: String)

fun main(args: Array<String>) {
    val handler = CuiXmlHandler()
    val factory = SAXParserFactory.newInstance()
    val parser = factory.newSAXParser()
    val list = File("/Users/a4d98zz/src/nlp/data/cpt-data/engine/cpt-cui/cpt.rules").readLines()
    for (line in list) {
        val arr = line.split(Regex("\\s+"))
        if (Regex("^\\d{6}$").matches(arr[0])) {
            val bodyPart = arr[5].replace("+", " ", true).trim().toUpperCase();
            if(bodyPart.length>0){
                val bodyParts = bodyPart.split(" ")
                for(part in bodyParts){
                    val p = part.replace("_"," ", true)
                    val cptSet = PART_CPT_MAP.get(p)?:HashSet<String>()
                    cptSet.add(arr[0])
                    PART_CPT_MAP.put(p, cptSet)
                }
            }
        }
    }
    parser.parse("/Users/a4d98zz/src/nlp/data/cpt-data/engine/cpt-cui/CMDictionaryBodyParts.xml",handler)

    CPT_CUI_MAP.toSortedMap().forEach {
        val cpt = it.key
        val cuiSet = it.value
        print("$cpt,")
        print("${cuiSet.first().bodyPart}")
        for(cui in cuiSet){
            print(",${cui.cui}")
        }
        println()
    }
}

class CuiXmlHandler : DefaultHandler() {
    var currentCui: String = ""

    override fun startElement(url: String, localName: String, fullName: String, attr: Attributes) {
        when (fullName) {
            "token" -> {
                currentCui = attr.getValue("cui")
            }
            "variant" -> {
                val key = attr.getValue("base").toUpperCase()
                if(PART_CPT_MAP.containsKey(key)){
                    val cptSet = PART_CPT_MAP.get(key)
                    for(cpt in cptSet!!){
                        val cuiSet = CPT_CUI_MAP.get(cpt)?:HashSet<CuiDesc>()
                        cuiSet.add(CuiDesc(currentCui, key))
                        CPT_CUI_MAP.put(cpt, cuiSet)
                    }
                }
            }
        }
    }
}

