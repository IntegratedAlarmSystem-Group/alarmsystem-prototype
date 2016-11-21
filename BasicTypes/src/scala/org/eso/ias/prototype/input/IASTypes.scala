package org.eso.ias.prototype.input

/**
 * The all and only possible types of the IAS MonitorPoints: * 
 * scala basic types and IAS custom types 
 *
 * There are all scala basic types including String  
 * even if at the present, I cannot see any valid 
 * use case for it.
 * 
 * Scala offers some support with this kind of thinks (like type tagging 
 * for instance) but I don't find it satisfactory for our purposes.
 * 
 * @see MonitorPoint
 * @see MonitorPointFactory
 * 
 * @author acaproni
 */
object IASTypes extends Enumeration {
  
  val LongType, 
    IntType, 
    ShortType, 
    ByteType, 
    DoubleType, 
    FloatType, 
    BooleanType, 
    CharType, 
    StringType, 
    AlarmType = Value
}