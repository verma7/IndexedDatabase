/**
 * Autogenerated by Thrift Compiler (0.8.0)
 *
 * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
 *  @generated
 */
package memdb.autogen;

import org.apache.thrift.scheme.IScheme;
import org.apache.thrift.scheme.SchemeFactory;
import org.apache.thrift.scheme.StandardScheme;

import org.apache.thrift.scheme.TupleScheme;
import org.apache.thrift.protocol.TTupleProtocol;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.EnumMap;
import java.util.Set;
import java.util.HashSet;
import java.util.EnumSet;
import java.util.Collections;
import java.util.BitSet;
import java.nio.ByteBuffer;
import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Index implements org.apache.thrift.TBase<Index, Index._Fields>, java.io.Serializable, Cloneable {
  private static final org.apache.thrift.protocol.TStruct STRUCT_DESC = new org.apache.thrift.protocol.TStruct("Index");

  private static final org.apache.thrift.protocol.TField ID_FIELD_DESC = new org.apache.thrift.protocol.TField("id", org.apache.thrift.protocol.TType.I16, (short)1);
  private static final org.apache.thrift.protocol.TField KEY_PREFIX_FIELD_DESC = new org.apache.thrift.protocol.TField("keyPrefix", org.apache.thrift.protocol.TType.STRING, (short)2);
  private static final org.apache.thrift.protocol.TField CONJUNCTIVE_PREDICATES_FIELD_DESC = new org.apache.thrift.protocol.TField("conjunctivePredicates", org.apache.thrift.protocol.TType.LIST, (short)3);
  private static final org.apache.thrift.protocol.TField SORT_ORDER_FIELD_DESC = new org.apache.thrift.protocol.TField("sortOrder", org.apache.thrift.protocol.TType.STRUCT, (short)4);

  private static final Map<Class<? extends IScheme>, SchemeFactory> schemes = new HashMap<Class<? extends IScheme>, SchemeFactory>();
  static {
    schemes.put(StandardScheme.class, new IndexStandardSchemeFactory());
    schemes.put(TupleScheme.class, new IndexTupleSchemeFactory());
  }

  public short id; // required
  public String keyPrefix; // required
  public List<Predicate> conjunctivePredicates; // required
  public SortOrder sortOrder; // required

  /** The set of fields this struct contains, along with convenience methods for finding and manipulating them. */
  public enum _Fields implements org.apache.thrift.TFieldIdEnum {
    ID((short)1, "id"),
    KEY_PREFIX((short)2, "keyPrefix"),
    CONJUNCTIVE_PREDICATES((short)3, "conjunctivePredicates"),
    SORT_ORDER((short)4, "sortOrder");

    private static final Map<String, _Fields> byName = new HashMap<String, _Fields>();

    static {
      for (_Fields field : EnumSet.allOf(_Fields.class)) {
        byName.put(field.getFieldName(), field);
      }
    }

    /**
     * Find the _Fields constant that matches fieldId, or null if its not found.
     */
    public static _Fields findByThriftId(int fieldId) {
      switch(fieldId) {
        case 1: // ID
          return ID;
        case 2: // KEY_PREFIX
          return KEY_PREFIX;
        case 3: // CONJUNCTIVE_PREDICATES
          return CONJUNCTIVE_PREDICATES;
        case 4: // SORT_ORDER
          return SORT_ORDER;
        default:
          return null;
      }
    }

    /**
     * Find the _Fields constant that matches fieldId, throwing an exception
     * if it is not found.
     */
    public static _Fields findByThriftIdOrThrow(int fieldId) {
      _Fields fields = findByThriftId(fieldId);
      if (fields == null) throw new IllegalArgumentException("Field " + fieldId + " doesn't exist!");
      return fields;
    }

    /**
     * Find the _Fields constant that matches name, or null if its not found.
     */
    public static _Fields findByName(String name) {
      return byName.get(name);
    }

    private final short _thriftId;
    private final String _fieldName;

    _Fields(short thriftId, String fieldName) {
      _thriftId = thriftId;
      _fieldName = fieldName;
    }

    public short getThriftFieldId() {
      return _thriftId;
    }

    public String getFieldName() {
      return _fieldName;
    }
  }

  // isset id assignments
  private static final int __ID_ISSET_ID = 0;
  private BitSet __isset_bit_vector = new BitSet(1);
  public static final Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> metaDataMap;
  static {
    Map<_Fields, org.apache.thrift.meta_data.FieldMetaData> tmpMap = new EnumMap<_Fields, org.apache.thrift.meta_data.FieldMetaData>(_Fields.class);
    tmpMap.put(_Fields.ID, new org.apache.thrift.meta_data.FieldMetaData("id", org.apache.thrift.TFieldRequirementType.REQUIRED, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.I16)));
    tmpMap.put(_Fields.KEY_PREFIX, new org.apache.thrift.meta_data.FieldMetaData("keyPrefix", org.apache.thrift.TFieldRequirementType.REQUIRED, 
        new org.apache.thrift.meta_data.FieldValueMetaData(org.apache.thrift.protocol.TType.STRING)));
    tmpMap.put(_Fields.CONJUNCTIVE_PREDICATES, new org.apache.thrift.meta_data.FieldMetaData("conjunctivePredicates", org.apache.thrift.TFieldRequirementType.REQUIRED, 
        new org.apache.thrift.meta_data.ListMetaData(org.apache.thrift.protocol.TType.LIST, 
            new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, Predicate.class))));
    tmpMap.put(_Fields.SORT_ORDER, new org.apache.thrift.meta_data.FieldMetaData("sortOrder", org.apache.thrift.TFieldRequirementType.REQUIRED, 
        new org.apache.thrift.meta_data.StructMetaData(org.apache.thrift.protocol.TType.STRUCT, SortOrder.class)));
    metaDataMap = Collections.unmodifiableMap(tmpMap);
    org.apache.thrift.meta_data.FieldMetaData.addStructMetaDataMap(Index.class, metaDataMap);
  }

  public Index() {
  }

  public Index(
    short id,
    String keyPrefix,
    List<Predicate> conjunctivePredicates,
    SortOrder sortOrder)
  {
    this();
    this.id = id;
    setIdIsSet(true);
    this.keyPrefix = keyPrefix;
    this.conjunctivePredicates = conjunctivePredicates;
    this.sortOrder = sortOrder;
  }

  /**
   * Performs a deep copy on <i>other</i>.
   */
  public Index(Index other) {
    __isset_bit_vector.clear();
    __isset_bit_vector.or(other.__isset_bit_vector);
    this.id = other.id;
    if (other.isSetKeyPrefix()) {
      this.keyPrefix = other.keyPrefix;
    }
    if (other.isSetConjunctivePredicates()) {
      List<Predicate> __this__conjunctivePredicates = new ArrayList<Predicate>();
      for (Predicate other_element : other.conjunctivePredicates) {
        __this__conjunctivePredicates.add(new Predicate(other_element));
      }
      this.conjunctivePredicates = __this__conjunctivePredicates;
    }
    if (other.isSetSortOrder()) {
      this.sortOrder = new SortOrder(other.sortOrder);
    }
  }

  public Index deepCopy() {
    return new Index(this);
  }

  @Override
  public void clear() {
    setIdIsSet(false);
    this.id = 0;
    this.keyPrefix = null;
    this.conjunctivePredicates = null;
    this.sortOrder = null;
  }

  public short getId() {
    return this.id;
  }

  public Index setId(short id) {
    this.id = id;
    setIdIsSet(true);
    return this;
  }

  public void unsetId() {
    __isset_bit_vector.clear(__ID_ISSET_ID);
  }

  /** Returns true if field id is set (has been assigned a value) and false otherwise */
  public boolean isSetId() {
    return __isset_bit_vector.get(__ID_ISSET_ID);
  }

  public void setIdIsSet(boolean value) {
    __isset_bit_vector.set(__ID_ISSET_ID, value);
  }

  public String getKeyPrefix() {
    return this.keyPrefix;
  }

  public Index setKeyPrefix(String keyPrefix) {
    this.keyPrefix = keyPrefix;
    return this;
  }

  public void unsetKeyPrefix() {
    this.keyPrefix = null;
  }

  /** Returns true if field keyPrefix is set (has been assigned a value) and false otherwise */
  public boolean isSetKeyPrefix() {
    return this.keyPrefix != null;
  }

  public void setKeyPrefixIsSet(boolean value) {
    if (!value) {
      this.keyPrefix = null;
    }
  }

  public int getConjunctivePredicatesSize() {
    return (this.conjunctivePredicates == null) ? 0 : this.conjunctivePredicates.size();
  }

  public java.util.Iterator<Predicate> getConjunctivePredicatesIterator() {
    return (this.conjunctivePredicates == null) ? null : this.conjunctivePredicates.iterator();
  }

  public void addToConjunctivePredicates(Predicate elem) {
    if (this.conjunctivePredicates == null) {
      this.conjunctivePredicates = new ArrayList<Predicate>();
    }
    this.conjunctivePredicates.add(elem);
  }

  public List<Predicate> getConjunctivePredicates() {
    return this.conjunctivePredicates;
  }

  public Index setConjunctivePredicates(List<Predicate> conjunctivePredicates) {
    this.conjunctivePredicates = conjunctivePredicates;
    return this;
  }

  public void unsetConjunctivePredicates() {
    this.conjunctivePredicates = null;
  }

  /** Returns true if field conjunctivePredicates is set (has been assigned a value) and false otherwise */
  public boolean isSetConjunctivePredicates() {
    return this.conjunctivePredicates != null;
  }

  public void setConjunctivePredicatesIsSet(boolean value) {
    if (!value) {
      this.conjunctivePredicates = null;
    }
  }

  public SortOrder getSortOrder() {
    return this.sortOrder;
  }

  public Index setSortOrder(SortOrder sortOrder) {
    this.sortOrder = sortOrder;
    return this;
  }

  public void unsetSortOrder() {
    this.sortOrder = null;
  }

  /** Returns true if field sortOrder is set (has been assigned a value) and false otherwise */
  public boolean isSetSortOrder() {
    return this.sortOrder != null;
  }

  public void setSortOrderIsSet(boolean value) {
    if (!value) {
      this.sortOrder = null;
    }
  }

  public void setFieldValue(_Fields field, Object value) {
    switch (field) {
    case ID:
      if (value == null) {
        unsetId();
      } else {
        setId((Short)value);
      }
      break;

    case KEY_PREFIX:
      if (value == null) {
        unsetKeyPrefix();
      } else {
        setKeyPrefix((String)value);
      }
      break;

    case CONJUNCTIVE_PREDICATES:
      if (value == null) {
        unsetConjunctivePredicates();
      } else {
        setConjunctivePredicates((List<Predicate>)value);
      }
      break;

    case SORT_ORDER:
      if (value == null) {
        unsetSortOrder();
      } else {
        setSortOrder((SortOrder)value);
      }
      break;

    }
  }

  public Object getFieldValue(_Fields field) {
    switch (field) {
    case ID:
      return Short.valueOf(getId());

    case KEY_PREFIX:
      return getKeyPrefix();

    case CONJUNCTIVE_PREDICATES:
      return getConjunctivePredicates();

    case SORT_ORDER:
      return getSortOrder();

    }
    throw new IllegalStateException();
  }

  /** Returns true if field corresponding to fieldID is set (has been assigned a value) and false otherwise */
  public boolean isSet(_Fields field) {
    if (field == null) {
      throw new IllegalArgumentException();
    }

    switch (field) {
    case ID:
      return isSetId();
    case KEY_PREFIX:
      return isSetKeyPrefix();
    case CONJUNCTIVE_PREDICATES:
      return isSetConjunctivePredicates();
    case SORT_ORDER:
      return isSetSortOrder();
    }
    throw new IllegalStateException();
  }

  @Override
  public boolean equals(Object that) {
    if (that == null)
      return false;
    if (that instanceof Index)
      return this.equals((Index)that);
    return false;
  }

  public boolean equals(Index that) {
    if (that == null)
      return false;

    boolean this_present_id = true;
    boolean that_present_id = true;
    if (this_present_id || that_present_id) {
      if (!(this_present_id && that_present_id))
        return false;
      if (this.id != that.id)
        return false;
    }

    boolean this_present_keyPrefix = true && this.isSetKeyPrefix();
    boolean that_present_keyPrefix = true && that.isSetKeyPrefix();
    if (this_present_keyPrefix || that_present_keyPrefix) {
      if (!(this_present_keyPrefix && that_present_keyPrefix))
        return false;
      if (!this.keyPrefix.equals(that.keyPrefix))
        return false;
    }

    boolean this_present_conjunctivePredicates = true && this.isSetConjunctivePredicates();
    boolean that_present_conjunctivePredicates = true && that.isSetConjunctivePredicates();
    if (this_present_conjunctivePredicates || that_present_conjunctivePredicates) {
      if (!(this_present_conjunctivePredicates && that_present_conjunctivePredicates))
        return false;
      if (!this.conjunctivePredicates.equals(that.conjunctivePredicates))
        return false;
    }

    boolean this_present_sortOrder = true && this.isSetSortOrder();
    boolean that_present_sortOrder = true && that.isSetSortOrder();
    if (this_present_sortOrder || that_present_sortOrder) {
      if (!(this_present_sortOrder && that_present_sortOrder))
        return false;
      if (!this.sortOrder.equals(that.sortOrder))
        return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    return 0;
  }

  public int compareTo(Index other) {
    if (!getClass().equals(other.getClass())) {
      return getClass().getName().compareTo(other.getClass().getName());
    }

    int lastComparison = 0;
    Index typedOther = (Index)other;

    lastComparison = Boolean.valueOf(isSetId()).compareTo(typedOther.isSetId());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetId()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.id, typedOther.id);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetKeyPrefix()).compareTo(typedOther.isSetKeyPrefix());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetKeyPrefix()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.keyPrefix, typedOther.keyPrefix);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetConjunctivePredicates()).compareTo(typedOther.isSetConjunctivePredicates());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetConjunctivePredicates()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.conjunctivePredicates, typedOther.conjunctivePredicates);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    lastComparison = Boolean.valueOf(isSetSortOrder()).compareTo(typedOther.isSetSortOrder());
    if (lastComparison != 0) {
      return lastComparison;
    }
    if (isSetSortOrder()) {
      lastComparison = org.apache.thrift.TBaseHelper.compareTo(this.sortOrder, typedOther.sortOrder);
      if (lastComparison != 0) {
        return lastComparison;
      }
    }
    return 0;
  }

  public _Fields fieldForId(int fieldId) {
    return _Fields.findByThriftId(fieldId);
  }

  public void read(org.apache.thrift.protocol.TProtocol iprot) throws org.apache.thrift.TException {
    schemes.get(iprot.getScheme()).getScheme().read(iprot, this);
  }

  public void write(org.apache.thrift.protocol.TProtocol oprot) throws org.apache.thrift.TException {
    schemes.get(oprot.getScheme()).getScheme().write(oprot, this);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("Index(");
    boolean first = true;

    sb.append("id:");
    sb.append(this.id);
    first = false;
    if (!first) sb.append(", ");
    sb.append("keyPrefix:");
    if (this.keyPrefix == null) {
      sb.append("null");
    } else {
      sb.append(this.keyPrefix);
    }
    first = false;
    if (!first) sb.append(", ");
    sb.append("conjunctivePredicates:");
    if (this.conjunctivePredicates == null) {
      sb.append("null");
    } else {
      sb.append(this.conjunctivePredicates);
    }
    first = false;
    if (!first) sb.append(", ");
    sb.append("sortOrder:");
    if (this.sortOrder == null) {
      sb.append("null");
    } else {
      sb.append(this.sortOrder);
    }
    first = false;
    sb.append(")");
    return sb.toString();
  }

  public void validate() throws org.apache.thrift.TException {
    // check for required fields
    // alas, we cannot check 'id' because it's a primitive and you chose the non-beans generator.
    if (keyPrefix == null) {
      throw new org.apache.thrift.protocol.TProtocolException("Required field 'keyPrefix' was not present! Struct: " + toString());
    }
    if (conjunctivePredicates == null) {
      throw new org.apache.thrift.protocol.TProtocolException("Required field 'conjunctivePredicates' was not present! Struct: " + toString());
    }
    if (sortOrder == null) {
      throw new org.apache.thrift.protocol.TProtocolException("Required field 'sortOrder' was not present! Struct: " + toString());
    }
  }

  private void writeObject(java.io.ObjectOutputStream out) throws java.io.IOException {
    try {
      write(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(out)));
    } catch (org.apache.thrift.TException te) {
      throw new java.io.IOException(te);
    }
  }

  private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException {
    try {
      // it doesn't seem like you should have to do this, but java serialization is wacky, and doesn't call the default constructor.
      __isset_bit_vector = new BitSet(1);
      read(new org.apache.thrift.protocol.TCompactProtocol(new org.apache.thrift.transport.TIOStreamTransport(in)));
    } catch (org.apache.thrift.TException te) {
      throw new java.io.IOException(te);
    }
  }

  private static class IndexStandardSchemeFactory implements SchemeFactory {
    public IndexStandardScheme getScheme() {
      return new IndexStandardScheme();
    }
  }

  private static class IndexStandardScheme extends StandardScheme<Index> {

    public void read(org.apache.thrift.protocol.TProtocol iprot, Index struct) throws org.apache.thrift.TException {
      org.apache.thrift.protocol.TField schemeField;
      iprot.readStructBegin();
      while (true)
      {
        schemeField = iprot.readFieldBegin();
        if (schemeField.type == org.apache.thrift.protocol.TType.STOP) { 
          break;
        }
        switch (schemeField.id) {
          case 1: // ID
            if (schemeField.type == org.apache.thrift.protocol.TType.I16) {
              struct.id = iprot.readI16();
              struct.setIdIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 2: // KEY_PREFIX
            if (schemeField.type == org.apache.thrift.protocol.TType.STRING) {
              struct.keyPrefix = iprot.readString();
              struct.setKeyPrefixIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 3: // CONJUNCTIVE_PREDICATES
            if (schemeField.type == org.apache.thrift.protocol.TType.LIST) {
              {
                org.apache.thrift.protocol.TList _list10 = iprot.readListBegin();
                struct.conjunctivePredicates = new ArrayList<Predicate>(_list10.size);
                for (int _i11 = 0; _i11 < _list10.size; ++_i11)
                {
                  Predicate _elem12; // required
                  _elem12 = new Predicate();
                  _elem12.read(iprot);
                  struct.conjunctivePredicates.add(_elem12);
                }
                iprot.readListEnd();
              }
              struct.setConjunctivePredicatesIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          case 4: // SORT_ORDER
            if (schemeField.type == org.apache.thrift.protocol.TType.STRUCT) {
              struct.sortOrder = new SortOrder();
              struct.sortOrder.read(iprot);
              struct.setSortOrderIsSet(true);
            } else { 
              org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
            }
            break;
          default:
            org.apache.thrift.protocol.TProtocolUtil.skip(iprot, schemeField.type);
        }
        iprot.readFieldEnd();
      }
      iprot.readStructEnd();

      // check for required fields of primitive type, which can't be checked in the validate method
      if (!struct.isSetId()) {
        throw new org.apache.thrift.protocol.TProtocolException("Required field 'id' was not found in serialized data! Struct: " + toString());
      }
      struct.validate();
    }

    public void write(org.apache.thrift.protocol.TProtocol oprot, Index struct) throws org.apache.thrift.TException {
      struct.validate();

      oprot.writeStructBegin(STRUCT_DESC);
      oprot.writeFieldBegin(ID_FIELD_DESC);
      oprot.writeI16(struct.id);
      oprot.writeFieldEnd();
      if (struct.keyPrefix != null) {
        oprot.writeFieldBegin(KEY_PREFIX_FIELD_DESC);
        oprot.writeString(struct.keyPrefix);
        oprot.writeFieldEnd();
      }
      if (struct.conjunctivePredicates != null) {
        oprot.writeFieldBegin(CONJUNCTIVE_PREDICATES_FIELD_DESC);
        {
          oprot.writeListBegin(new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRUCT, struct.conjunctivePredicates.size()));
          for (Predicate _iter13 : struct.conjunctivePredicates)
          {
            _iter13.write(oprot);
          }
          oprot.writeListEnd();
        }
        oprot.writeFieldEnd();
      }
      if (struct.sortOrder != null) {
        oprot.writeFieldBegin(SORT_ORDER_FIELD_DESC);
        struct.sortOrder.write(oprot);
        oprot.writeFieldEnd();
      }
      oprot.writeFieldStop();
      oprot.writeStructEnd();
    }

  }

  private static class IndexTupleSchemeFactory implements SchemeFactory {
    public IndexTupleScheme getScheme() {
      return new IndexTupleScheme();
    }
  }

  private static class IndexTupleScheme extends TupleScheme<Index> {

    @Override
    public void write(org.apache.thrift.protocol.TProtocol prot, Index struct) throws org.apache.thrift.TException {
      TTupleProtocol oprot = (TTupleProtocol) prot;
      oprot.writeI16(struct.id);
      oprot.writeString(struct.keyPrefix);
      {
        oprot.writeI32(struct.conjunctivePredicates.size());
        for (Predicate _iter14 : struct.conjunctivePredicates)
        {
          _iter14.write(oprot);
        }
      }
      struct.sortOrder.write(oprot);
    }

    @Override
    public void read(org.apache.thrift.protocol.TProtocol prot, Index struct) throws org.apache.thrift.TException {
      TTupleProtocol iprot = (TTupleProtocol) prot;
      struct.id = iprot.readI16();
      struct.setIdIsSet(true);
      struct.keyPrefix = iprot.readString();
      struct.setKeyPrefixIsSet(true);
      {
        org.apache.thrift.protocol.TList _list15 = new org.apache.thrift.protocol.TList(org.apache.thrift.protocol.TType.STRUCT, iprot.readI32());
        struct.conjunctivePredicates = new ArrayList<Predicate>(_list15.size);
        for (int _i16 = 0; _i16 < _list15.size; ++_i16)
        {
          Predicate _elem17; // required
          _elem17 = new Predicate();
          _elem17.read(iprot);
          struct.conjunctivePredicates.add(_elem17);
        }
      }
      struct.setConjunctivePredicatesIsSet(true);
      struct.sortOrder = new SortOrder();
      struct.sortOrder.read(iprot);
      struct.setSortOrderIsSet(true);
    }
  }

}

