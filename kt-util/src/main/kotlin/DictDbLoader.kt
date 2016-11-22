package com.mmm.his.nlp.rapidcontent.util

import org.xml.sax.Attributes
import org.xml.sax.helpers.DefaultHandler
import java.io.File
import java.nio.charset.Charset
import java.util.*
import javax.xml.parsers.SAXParserFactory
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction


var propMap:MutableMap<String, Integer?> = mutableMapOf()
var propValueMap:MutableMap<String, Integer?> = mutableMapOf()
var varStrMap : MutableMap<String, Integer?> = mutableMapOf()
var typeId = 0;

object Dictionary : Table("tblDictionary") {
    val id = integer("id").autoIncrement().primaryKey()
    val name = varchar("name", length = 50).uniqueIndex()
}

object VarStr : Table("tblVariantString") {
    val id = integer("id").autoIncrement().primaryKey()
    val value = varchar("value", length = 1000).uniqueIndex()
}

object Variant : Table("tblVariant") {
    val id = integer("id").autoIncrement().primaryKey()
    val dictId = (integer("dictionaryId") references Dictionary.id)
    val varStrId = (integer("variantStringId") references Dictionary.id)
    val priority = integer("priority")
    val lastUpdatedOn = datetime("lastUpdatedOn")
}

object VariantProps : Table("tblEntryProperties") {
    val varId = (integer("variantId") references Variant.id)
    val propValueId = (integer("propertyValueId") references PropValue.id)
}

object PropValue : Table("tblPropertyValue") {
    val id = integer("id").autoIncrement().primaryKey()
    val propId = (integer("propertyId") references PropName.id)
    val value = varchar("value", 50)
}

object PropName : Table("tblProperty") {
    val id = integer("id").autoIncrement().primaryKey()
    val name = varchar("name", 50).uniqueIndex()
}

fun main(args: Array<String>) {
    Database.connect("jdbc:mysql://localhost:3306/RapidContent?" +
            "createDatabaseIfNotExist=true",driver = "com.mysql.jdbc.Driver", user="root")
    val handler = DictXmlHandler()
    val factory = SAXParserFactory.newInstance()
    val parser = factory.newSAXParser()
    val list = File("/Users/a4d98zz/tmp/tmp/com/mmm/his/nlp/dictionary/resources")
            .listFiles()
            .filter { it.name.endsWith(".xml") }
            .toList()
    for (file in list) {
        println("parsing " + file.name + " ...")
        val currentFile = file.nameWithoutExtension;
        transaction {
            typeId = Dictionary.insert {
                it[name] = currentFile
            } get Dictionary.id
        }
        parser.parse(file, handler)
    }

    /*val varFile = File("/Users/a4d98zz/tmp/varstr.txt").printWriter(Charset.forName("UTF-8"))
    val cuiFile = File("/Users/a4d98zz/tmp/cuistr.txt").printWriter(Charset.forName("UTF-8"))

    for(str in varStrSet){
        varFile.println(str)
    }

    for(str in cuiSet){
        cuiFile.println(str)
    }*/

}


class DictXmlHandler : DefaultHandler() {

    override fun startElement(url: String, localName: String, fullName: String, attrs: Attributes) {
        var propValueIdSet:MutableSet<Integer> = mutableSetOf()
        when (fullName) {
            "token" -> {
                propValueIdSet = mutableSetOf()
                val len = attrs.length
                for(i in 0..len-1){
                    val aname = attrs.getQName(i)
                    val avalue = attrs.getValue(i)

                    var propNameId = propMap.get(aname)

                    transaction {
                        if (propNameId == null) {
                            propNameId = Integer(PropName.insert {
                                it[name] = aname
                            } get PropName.id)
                            propMap.put(aname, propNameId)
                        }

                        var propValueId = propValueMap.get(avalue + propNameId)

                        if (propValueId == null) {
                            propValueId = Integer(PropValue.insert {
                                it[propId] = propNameId?.toInt()
                                it[value] = avalue
                            } get PropValue.id)
                            propValueMap.put(avalue + propNameId, propValueId)
                        }
                        propValueIdSet.add(propValueId)
                    }
                }
            }
            "variant" -> {
                val varStr = attrs.getValue("base")
                transaction {

                    var aVarStrId = varStrMap.get(varStr)

                    if(aVarStrId == null) {
                        aVarStrId = Integer(VarStr.insert{
                            it[value] = varStr
                        } get VarStr.id)
                        varStrMap.put(varStr, aVarStrId)
                    }

                    val aVarId = Variant.insert {
                        it[varStrId] = aVarStrId?.toInt()
                        it[dictId] = typeId
                        it[priority] = 5
                    } get Variant.id

                    for(aPropValueId in propValueIdSet) {
                        VariantProps.insert {
                            it[varId] = aVarId
                            it[propValueId] = aPropValueId.toInt()
                        }
                    }
                }

            }
        }
    }
}

