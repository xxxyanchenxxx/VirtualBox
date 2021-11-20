#ifndef __MONO_FUNC_H
#define __MONO_FUNC_H

#include <unistd.h>
#include <elf.h>

#define    __int8 char
#define    __int16 short int
#define    __int32  int
#define    __int64  long long
#define     out
#define    byte uint8_t
#define    ushort uint16_t

typedef int8_t gint8;
typedef uint8_t guint8;
typedef int16_t gint16;
typedef uint16_t guint16;
typedef int32_t gint32;
typedef uint32_t guint32;
typedef int64_t gint64;
typedef uint64_t guint64;
typedef uint16_t gunichar2;

typedef int gboolean;
typedef int gint;
typedef unsigned int guint;
typedef short gshort;
typedef unsigned short gushort;
typedef long glong;
typedef uint64_t gulong;
typedef void *gpointer;
typedef const void *gconstpointer;
typedef char gchar;
typedef unsigned char guchar;

typedef __int16 gint16;
typedef __int32 gint32;
typedef float gfloat;
typedef double gdouble;
typedef unsigned int gsize;
typedef size_t regmask_t;
typedef unsigned long ulong;
typedef guint32 mono_array_size_t;
typedef gpointer MonoRuntimeGenericContext;

#define SOINFO_NAME_LEN 128
#define MONO_PUBLIC_KEY_TOKEN_LENGTH    17
#define MONO_ZERO_LEN_ARRAY 1

typedef struct _Soinfo {
    char name[SOINFO_NAME_LEN];
    const Elf32_Phdr *phdr;
    size_t phnum;
    Elf32_Addr entry;
    Elf32_Addr base;
    unsigned size;

    uint32_t unused1;  // DO NOT USE, maintained for compatibility.

    Elf32_Dyn *dynamic;

    uint32_t unused2; // DO NOT USE, maintained for compatibility
    uint32_t unused3; // DO NOT USE, maintained for compatibility

    _Soinfo *next;
    unsigned flags;

    const char *strtab;
    Elf32_Sym *symtab;

    size_t nbucket;
    size_t nchain;
    unsigned *bucket;
    unsigned *chain;

    unsigned *plt_got;

    Elf32_Rel *plt_rel;
    size_t plt_rel_count;

    Elf32_Rel *rel;
    size_t rel_count;
} Soinfo, *pSoinfo;

typedef enum {
    MONO_IMAGE_OK,
    MONO_IMAGE_ERROR_ERRNO,
    MONO_IMAGE_MISSING_ASSEMBLYREF,
    MONO_IMAGE_IMAGE_INVALID
} MonoImageOpenStatus;

typedef struct _MonoClass MonoClass;
typedef struct _MonoDomain MonoDomain;
typedef struct _MonoMethod MonoMethod;
typedef struct _MonoImage MonoImage;
typedef struct _MonoAssembly MonoAssembly;
typedef struct _MonoTableInfo MonoTableInfo;
typedef struct _GHashTable GHashTable;
typedef struct _MonoInternalHashTable MonoInternalHashTable;
typedef struct _WapiCriticalSection WapiCriticalSection;
typedef WapiCriticalSection CRITICAL_SECTION;
typedef CRITICAL_SECTION mono_mutex_t;
typedef struct _MonoPropertyHash MonoPropertyHash;
typedef struct _MonoDllMap MonoDllMap;
typedef struct _MonoClassField MonoClassField;
typedef struct _MonoType MonoType;
typedef gint32 mono_array_lower_bound_t;
typedef struct _MonoVTable MonoVTable;

#define USE_UINT8_BIT_FIELD(type, field) type field

typedef struct _Slot Slot;

typedef void     (*GFunc)(gpointer data, gpointer user_data);

typedef gint     (*GCompareFunc)(gconstpointer a, gconstpointer b);

typedef gint     (*GCompareDataFunc)(gconstpointer a, gconstpointer b, gpointer user_data);

typedef void     (*GHFunc)(gpointer key, gpointer value, gpointer user_data);

typedef gboolean (*GHRFunc)(gpointer key, gpointer value, gpointer user_data);

typedef void     (*GDestroyNotify)(gpointer data);

typedef guint    (*GHashFunc)(gconstpointer key);

typedef gboolean (*GEqualFunc)(gconstpointer a, gconstpointer b);

typedef void     (*GFreeFunc)(gpointer data);

typedef gpointer (*MonoInternalHashKeyExtractFunc)(gpointer value);

typedef gpointer *(*MonoInternalHashNextValueFunc)(gpointer value);

typedef void *MonoMemPool;
typedef struct _GPtrArray GPtrArray;


typedef struct MonoGenericParam {
    int value;
} MonoGenericParam;

typedef struct MonoGenericClass {
    int value;
} MonoGenericClass;


typedef struct MonoMethodSignature {
    int value;
} MonoMethodSignature;


typedef struct MonoArrayType {
    int value;
} MonoArrayType;


typedef struct MonoMarshalType {
    int value;
} MonoMarshalType;


typedef struct MonoGenericContainer {
    int value;
} MonoGenericContainer;

typedef struct MonoClassRuntimeInfo {
    int value;
} MonoClassRuntimeInfo;

typedef struct MonoClassExt {
    int value;
} MonoClassExt;

typedef struct MonoInst {
    int value;
} MonoInst;

typedef struct MonoMethodVar {
    int value;
} MonoMethodVar;


typedef struct MonoBasicBlock {
    int value;
} MonoBasicBlock;


typedef struct MonoJumpInfo {
    int value;
} MonoJumpInfo;

typedef struct MonoJitInfo {
    int value;
} MonoJitInfo;

typedef struct MonoJitDynamicMethodInfo {
    int value;
} MonoJitDynamicMethodInfo;


typedef struct MonoRegState {
    int value;
} MonoRegState;

typedef struct MonoSpillInfo {
    int value;
} MonoSpillInfo;


typedef struct MonoGenericContext {
    int value;
} MonoGenericContext;


typedef struct GList {
    int value;
} GList;


typedef struct MonoGenericSharingContext {
    int value;
} MonoGenericSharingContext;


typedef struct MonoProfileCoverageInfo {
    int value;
} MonoProfileCoverageInfo;

typedef struct MonoSeqPointInfo {
    int value;
} MonoSeqPointInfo;


typedef struct MonoCompileArch {
    gint32 lmf_offset;
    gint32 localloc_offset;
    gint32 reg_save_area_offset;
    gint32 args_save_area_offset;
    gint32 stack_size;        // Allocated stack size in bytes
    gint32 params_stack_size;  // Stack size reserved for call params by this compile method

    gpointer got_data;
    glong bwx;
} MonoCompileArch;


struct _GPtrArray {
    gpointer *pdata;
    guint len;
};

struct _Slot {
    gpointer key;
    gpointer value;
    Slot *next;
};

typedef struct {
    const char *data;
    guint32 size;
} MonoStreamHeader;

typedef struct _GSList GSList;
struct _GSList {
    gpointer data;
    GSList *next;
};

struct _MonoDllMap {
    char *dll;
    char *target;
    char *func;
    char *target_func;
    MonoDllMap *next;
};

typedef struct {
    gsize size;
    gsize flags;
    gsize data[MONO_ZERO_LEN_ARRAY];
} MonoBitSet;


struct _MonoInternalHashTable {
    GHashFunc hash_func;
    MonoInternalHashKeyExtractFunc key_extract;
    MonoInternalHashNextValueFunc next_value;
    gint size;
    gint num_entries;
    gpointer *table;
};

struct _WapiCriticalSection {
    guint32 depth;
    _WapiCriticalSection *mutex;
};


struct _MonoClassField {
    /* Type of the field */
    MonoType *type;

    const char *name;

    /* Type where the field was defined */
    MonoClass *parent;

    /*
     * Offset where this field is stored; if it is an instance
     * field, it's the offset from the start of the object, if
     * it's static, it's from the start of the memory chunk
     * allocated for statics for the class.
     * For special static fields, this is set to -1 during vtable construction.
     */
    int offset;
};

typedef enum {
    MONO_TABLE_MODULE,
    MONO_TABLE_TYPEREF,
    MONO_TABLE_TYPEDEF,
    MONO_TABLE_FIELD_POINTER,
    MONO_TABLE_FIELD,
    MONO_TABLE_METHOD_POINTER,
    MONO_TABLE_METHOD,
    MONO_TABLE_PARAM_POINTER,
    MONO_TABLE_PARAM,
    MONO_TABLE_INTERFACEIMPL,
    MONO_TABLE_MEMBERREF, /* 0xa */
    MONO_TABLE_CONSTANT,
    MONO_TABLE_CUSTOMATTRIBUTE,
    MONO_TABLE_FIELDMARSHAL,
    MONO_TABLE_DECLSECURITY,
    MONO_TABLE_CLASSLAYOUT,
    MONO_TABLE_FIELDLAYOUT, /* 0x10 */
    MONO_TABLE_STANDALONESIG,
    MONO_TABLE_EVENTMAP,
    MONO_TABLE_EVENT_POINTER,
    MONO_TABLE_EVENT,
    MONO_TABLE_PROPERTYMAP,
    MONO_TABLE_PROPERTY_POINTER,
    MONO_TABLE_PROPERTY,
    MONO_TABLE_METHODSEMANTICS,
    MONO_TABLE_METHODIMPL,
    MONO_TABLE_MODULEREF, /* 0x1a */
    MONO_TABLE_TYPESPEC,
    MONO_TABLE_IMPLMAP,
    MONO_TABLE_FIELDRVA,
    MONO_TABLE_UNUSED6,
    MONO_TABLE_UNUSED7,
    MONO_TABLE_ASSEMBLY, /* 0x20 */
    MONO_TABLE_ASSEMBLYPROCESSOR,
    MONO_TABLE_ASSEMBLYOS,
    MONO_TABLE_ASSEMBLYREF,
    MONO_TABLE_ASSEMBLYREFPROCESSOR,
    MONO_TABLE_ASSEMBLYREFOS,
    MONO_TABLE_FILE,
    MONO_TABLE_EXPORTEDTYPE,
    MONO_TABLE_MANIFESTRESOURCE,
    MONO_TABLE_NESTEDCLASS,
    MONO_TABLE_GENERICPARAM, /* 0x2a */
    MONO_TABLE_METHODSPEC,
    MONO_TABLE_GENERICPARAMCONSTRAINT

#define MONO_TABLE_LAST MONO_TABLE_GENERICPARAMCONSTRAINT
#define MONO_TABLE_NUM (MONO_TABLE_LAST + 1)

} MonoMetaTableEnum;

typedef enum {
    MONO_TYPE_END = 0x00,       /* End of List */
    MONO_TYPE_VOID = 0x01,
    MONO_TYPE_BOOLEAN = 0x02,
    MONO_TYPE_CHAR = 0x03,
    MONO_TYPE_I1 = 0x04,
    MONO_TYPE_U1 = 0x05,
    MONO_TYPE_I2 = 0x06,
    MONO_TYPE_U2 = 0x07,
    MONO_TYPE_I4 = 0x08,
    MONO_TYPE_U4 = 0x09,
    MONO_TYPE_I8 = 0x0a,
    MONO_TYPE_U8 = 0x0b,
    MONO_TYPE_R4 = 0x0c,
    MONO_TYPE_R8 = 0x0d,
    MONO_TYPE_STRING = 0x0e,
    MONO_TYPE_PTR = 0x0f,       /* arg: <type> token */
    MONO_TYPE_BYREF = 0x10,       /* arg: <type> token */
    MONO_TYPE_VALUETYPE = 0x11,       /* arg: <type> token */
    MONO_TYPE_CLASS = 0x12,       /* arg: <type> token */
    MONO_TYPE_VAR = 0x13,       /* number */
    MONO_TYPE_ARRAY = 0x14,       /* type, rank, boundsCount, bound1, loCount, lo1 */
    MONO_TYPE_GENERICINST = 0x15,       /* <type> <type-arg-count> <type-1> \x{2026} <type-n> */
    MONO_TYPE_TYPEDBYREF = 0x16,
    MONO_TYPE_I = 0x18,
    MONO_TYPE_U = 0x19,
    MONO_TYPE_FNPTR = 0x1b,          /* arg: full method signature */
    MONO_TYPE_OBJECT = 0x1c,
    MONO_TYPE_SZARRAY = 0x1d,       /* 0-based one-dim-array */
    MONO_TYPE_MVAR = 0x1e,       /* number */
    MONO_TYPE_CMOD_REQD = 0x1f,       /* arg: typedef or typeref token */
    MONO_TYPE_CMOD_OPT = 0x20,       /* optional arg: typedef or typref token */
    MONO_TYPE_INTERNAL = 0x21,       /* CLR internal type */

    MONO_TYPE_MODIFIER = 0x40,       /* Or with the following types */
    MONO_TYPE_SENTINEL = 0x41,       /* Sentinel for varargs method signature */
    MONO_TYPE_PINNED = 0x45,       /* Local var that points to pinned object */

    MONO_TYPE_ENUM = 0x55        /* an enumeration */
} MonoTypeEnum;


typedef struct {
    unsigned int required : 1;
    unsigned int token    : 31;
} MonoCustomMod;


struct _MonoType {
    union {
        MonoClass *klass; /* for VALUETYPE and CLASS */
        MonoType *type;   /* for PTR */
        MonoArrayType *array; /* for ARRAY */
        MonoMethodSignature *method;
        MonoGenericParam *generic_param; /* for VAR and MVAR */
        MonoGenericClass *generic_class; /* for GENERICINST */
    } data;
    unsigned int attrs    : 16; /* param attributes or field flags */
    MonoTypeEnum type     : 8;
    unsigned int num_mods : 6;  /* max 64 modifiers follow at the end */
    unsigned int byref    : 1;
    unsigned int pinned   : 1;  /* valid when included in a local var signature */
    MonoCustomMod modifiers[MONO_ZERO_LEN_ARRAY]; /* this may grow */
};

struct _GHashTable {
    GHashFunc hash_func;
    GEqualFunc key_equal_func;

    Slot **table;
    int table_size;
    int in_use;
    int threshold;
    int last_rehash;
    GDestroyNotify value_destroy_func, key_destroy_func;
};

struct _MonoTableInfo {
    const char *base;
    guint rows     : 24;
    guint row_size : 8;

    /*
     * Tables contain up to 9 columns and the possible sizes of the
     * fields in the documentation are 1, 2 and 4 bytes.  So we
     * can encode in 2 bits the size.
     *
     * A 32 bit value can encode the resulting size
     *
     * The top eight bits encode the number of columns in the table.
     * we only need 4, but 8 is aligned no shift required.
     */
    guint32 size_bitfield;
};


struct _MonoPropertyHash {
    /* We use one hash table per property */
    GHashTable *hashes;
};


typedef struct {
    const char *name;
    const char *culture;
    const char *hash_value;
    const guint8 *public_key;
    // string of 16 hex chars + 1 NULL
    guchar public_key_token[MONO_PUBLIC_KEY_TOKEN_LENGTH];
    guint32 hash_alg;
    guint32 hash_len;
    guint32 flags;
    guint16 major, minor, build, revision;
} MonoAssemblyName;

struct _MonoAssembly {
    /*
     * The number of appdomains which have this assembly loaded plus the number of
     * assemblies referencing this assembly through an entry in their image->references
     * arrays. The later is needed because entries in the image->references array
     * might point to assemblies which are only loaded in some appdomains, and without
     * the additional reference, they can be freed at any time.
     * The ref_count is initially 0.
     */
    int ref_count; /* use atomic operations only */
    char *basedir;
    MonoAssemblyName aname;
    MonoImage *image;
    GSList *friend_assembly_names; /* Computed by mono_assembly_load_friends () */
    guint8 friend_assembly_names_inited;
    guint8 in_gac;
    guint8 dynamic;
    guint8 corlib_internal;
    gboolean ref_only;
    /* security manager flags (one bit is for lazy initialization) */
    guint32 ecma:2;        /* Has the ECMA key */
    guint32 aptc:2;        /* Has the [AllowPartiallyTrustedCallers] attributes */
    guint32 fulltrust:2;    /* Has FullTrust permission */
    guint32 unmanaged:2;    /* Has SecurityPermissionFlag.UnmanagedCode permission */
    guint32 skipverification:2;    /* Has SecurityPermissionFlag.SkipVerification permission */
};


struct _MonoImage {
    /*
     * The number of assemblies which reference this MonoImage though their 'image'
     * field plus the number of images which reference this MonoImage through their
     * 'modules' field, plus the number of threads holding temporary references to
     * this image between calls of mono_image_open () and mono_image_close ().
     */
    int ref_count;
    void *raw_data_handle;
    char *raw_data;
    guint32 raw_data_len;
    guint8 raw_buffer_used    : 1;
    guint8 raw_data_allocated : 1;

#ifdef USE_COREE
    /* Module was loaded using LoadLibrary. */
    guint8 is_module_handle : 1;

    /* Module entry point is _CorDllMain. */
    guint8 has_entry_point : 1;
#endif

    /* Whenever this is a dynamically emitted module */
    guint8 dynamic : 1;

    /* Whenever this is a reflection only image */
    guint8 ref_only : 1;

    /* Whenever this image contains uncompressed metadata */
    guint8 uncompressed_metadata : 1;

    guint8 checked_module_cctor : 1;
    guint8 has_module_cctor : 1;

    guint8 idx_string_wide : 1;
    guint8 idx_guid_wide : 1;
    guint8 idx_blob_wide : 1;

    /* Whenever this image is considered as platform code for the CoreCLR security model */
    guint8 core_clr_platform_code : 1;

    char *name;
    const char *assembly_name;
    const char *module_name;
    char *version;
    gint16 md_version_major, md_version_minor;
    char *guid;
    void *image_info;
    MonoMemPool *mempool; /*protected by the image lock*/

    char *raw_metadata;

    MonoStreamHeader heap_strings;
    MonoStreamHeader heap_us;
    MonoStreamHeader heap_blob;
    MonoStreamHeader heap_guid;
    MonoStreamHeader heap_tables;

    const char *tables_base;

    /**/
    MonoTableInfo tables[MONO_TABLE_NUM];

    /*
     * references is initialized only by using the mono_assembly_open
     * function, and not by using the lowlevel mono_image_open.
     *
     * It is NULL terminated.
     */
    MonoAssembly **references;

    MonoImage **modules;
    guint32 module_count;
    gboolean *modules_loaded;

    MonoImage **files;

    gpointer aot_module;

    /*
     * The Assembly this image was loaded from.
     */
    MonoAssembly *assembly;

    /*
     * Indexed by method tokens and typedef tokens.
     */
    GHashTable *method_cache; /*protected by the image lock*/
    MonoInternalHashTable class_cache;

    /* Indexed by memberref + methodspec tokens */
    GHashTable *methodref_cache; /*protected by the image lock*/

    /*
     * Indexed by fielddef and memberref tokens
     */
    GHashTable *field_cache;

    /* indexed by typespec tokens. */
    GHashTable *typespec_cache;
    /* indexed by token */
    GHashTable *memberref_signatures;
    GHashTable *helper_signatures;

    /* Indexed by blob heap indexes */
    GHashTable *method_signatures;

    /*
     * Indexes namespaces to hash tables that map class name to typedef token.
     */
    GHashTable *name_cache;  /*protected by the image lock*/

    /*
     * Indexed by MonoClass
     */
    GHashTable *array_cache;
    GHashTable *ptr_cache;

    GHashTable *szarray_cache;
    /* This has a separate lock to improve scalability */
    CRITICAL_SECTION szarray_cache_lock;

    /*
     * indexed by MonoMethodSignature
     */
    GHashTable *delegate_begin_invoke_cache;
    GHashTable *delegate_end_invoke_cache;
    GHashTable *delegate_invoke_cache;
    GHashTable *runtime_invoke_cache;

    /*
     * indexed by SignatureMethodPair
     */
    GHashTable *delegate_abstract_invoke_cache;

    /*
     * indexed by MonoMethod pointers
     */
    GHashTable *runtime_invoke_direct_cache;
    GHashTable *runtime_invoke_vcall_cache;
    GHashTable *managed_wrapper_cache;
    GHashTable *native_wrapper_cache;
    GHashTable *native_wrapper_aot_cache;
    GHashTable *remoting_invoke_cache;
    GHashTable *synchronized_cache;
    GHashTable *unbox_wrapper_cache;
    GHashTable *cominterop_invoke_cache;
    GHashTable *cominterop_wrapper_cache; /* LOCKING: marshal lock */
    GHashTable *thunk_invoke_cache;

    /*
     * indexed by MonoClass pointers
     */
    GHashTable *ldfld_wrapper_cache;
    GHashTable *ldflda_wrapper_cache;
    GHashTable *stfld_wrapper_cache;
    GHashTable *isinst_cache;
    GHashTable *castclass_cache;
    GHashTable *proxy_isinst_cache;
    GHashTable *rgctx_template_hash; /* LOCKING: templates lock */

    /*
     * indexed by token and MonoGenericContext pointer
     */
    GHashTable *generic_class_cache;

    /* Contains rarely used fields of runtime structures belonging to this image */
    MonoPropertyHash *property_hash;

    void *reflection_info;

    /*
     * user_info is a public field and is not touched by the
     * metadata engine
     */
    void *user_info;

    /* dll map entries */
    MonoDllMap *dll_map;

    /* interfaces IDs from this image */
    MonoBitSet *interface_bitset;

    GSList *reflection_info_unregister_classes;

    /*
     * No other runtime locks must be taken while holding this lock.
     * It's meant to be used only to mutate and query structures part of this image.
     */
    CRITICAL_SECTION lock;
};

typedef struct {
    MonoVTable *vtable;
    void *synchronisation;
} MonoObject;

struct _MonoClass {
    /* element class for arrays and enum basetype for enums */
    MonoClass *element_class;
    /* used for subtype checks */
    MonoClass *cast_class;

    /* for fast subtype checks */
    MonoClass **supertypes;
    guint16 idepth;

    /* array dimension */
    guint8 rank;

    int instance_size; /* object instance size */

    USE_UINT8_BIT_FIELD(guint, inited          :
        1);
    /* We use init_pending to detect cyclic calls to mono_class_init */
    USE_UINT8_BIT_FIELD(guint, init_pending    :
        1);

    /* A class contains static and non static data. Static data can be
     * of the same type as the class itselfs, but it does not influence
     * the instance size of the class. To avoid cyclic calls to
     * mono_class_init (from mono_class_instance_size ()) we first
     * initialise all non static fields. After that we set size_inited
     * to 1, because we know the instance size now. After that we
     * initialise all static fields.
     */
    USE_UINT8_BIT_FIELD(guint, size_inited     :
        1);
    USE_UINT8_BIT_FIELD(guint, valuetype       :
        1); /* derives from System.ValueType */
    USE_UINT8_BIT_FIELD(guint, enumtype        :
        1); /* derives from System.Enum */
    USE_UINT8_BIT_FIELD(guint, blittable       :
        1); /* class is blittable */
    USE_UINT8_BIT_FIELD(guint, unicode         :
        1); /* class uses unicode char when marshalled */
    USE_UINT8_BIT_FIELD(guint, wastypebuilder  :
        1); /* class was created at runtime from a TypeBuilder */
    /* next byte */
    guint8 min_align;
    /* next byte */
    USE_UINT8_BIT_FIELD(guint, packing_size    :
        4);
    /* still 4 bits free */
    /* next byte */
    USE_UINT8_BIT_FIELD(guint, ghcimpl         :
        1); /* class has its own GetHashCode impl */
    USE_UINT8_BIT_FIELD(guint, has_finalize    :
        1); /* class has its own Finalize impl */
    USE_UINT8_BIT_FIELD(guint, marshalbyref    :
        1); /* class is a MarshalByRefObject */
    USE_UINT8_BIT_FIELD(guint, contextbound    :
        1); /* class is a ContextBoundObject */
    USE_UINT8_BIT_FIELD(guint, delegate        :
        1); /* class is a Delegate */
    USE_UINT8_BIT_FIELD(guint, gc_descr_inited :
        1); /* gc_descr is initialized */
    USE_UINT8_BIT_FIELD(guint, has_cctor       :
        1); /* class has a cctor */
    USE_UINT8_BIT_FIELD(guint, has_references  :
        1); /* it has GC-tracked references in the instance */
    /* next byte */
    USE_UINT8_BIT_FIELD(guint, has_static_refs :
        1); /* it has static fields that are GC-tracked */
    USE_UINT8_BIT_FIELD(guint, no_special_static_fields :
        1); /* has no thread/context static fields */
    /* directly or indirectly derives from ComImport attributed class.
     * this means we need to create a proxy for instances of this class
     * for COM Interop. set this flag on loading so all we need is a quick check
     * during object creation rather than having to traverse supertypes
     */
    USE_UINT8_BIT_FIELD(guint, is_com_object   :
        1);
    USE_UINT8_BIT_FIELD(guint, nested_classes_inited :
        1); /* Whenever nested_class is initialized */
    USE_UINT8_BIT_FIELD(guint, interfaces_inited :
        1); /* interfaces is initialized */
    USE_UINT8_BIT_FIELD(guint, simd_type       :
        1); /* class is a simd intrinsic type */
    USE_UINT8_BIT_FIELD(guint, is_generic      :
        1); /* class is a generic type definition */
    USE_UINT8_BIT_FIELD(guint, is_inflated     :
        1); /* class is a generic instance */

    guint8 exception_type;    /* MONO_EXCEPTION_* */

    /* Additional information about the exception */
    /* Stored as property MONO_CLASS_PROP_EXCEPTION_DATA */
    //void       *exception_data;

    MonoClass *parent;
    MonoClass *nested_in;

    MonoImage *image;
    const char *name;
    const char *name_space;

    guint32 type_token;
    int vtable_size; /* number of slots */

    guint16 interface_count;
    guint16 interface_id;        /* unique inderface id (for interfaces) */
    guint16 max_interface_id;

    guint16 interface_offsets_count;
    MonoClass **interfaces_packed;
    guint16 *interface_offsets_packed;
    guint8 *interface_bitmap;

    MonoClass **interfaces;

    union {
        int class_size; /* size of area for static fields */
        int element_size; /* for array types */
        int generic_param_token; /* for generic param types, both var and mvar */
    } sizes;

    /*
     * From the TypeDef table
     */
    guint32 flags;
    struct {
        guint32 first, count;
    } field, method;

    /* loaded on demand */
    MonoMarshalType *marshal_info;

    /*
     * Field information: Type and location from object base
     */
    MonoClassField *fields;

    MonoMethod **methods;

    /* used as the type of the this argument and when passing the arg by value */
    MonoType this_arg;
    MonoType byval_arg;

    MonoGenericClass *generic_class;
    MonoGenericContainer *generic_container;

    void *reflection_info;

    void *gc_descr;

    MonoClassRuntimeInfo *runtime_info;

    /* next element in the class_cache hash list (in MonoImage) */
    MonoClass *next_class_cache;

    /* Generic vtable. Initialized by a call to mono_class_setup_vtable () */
    MonoMethod **vtable;

    /* Rarely used fields of classes */
    MonoClassExt *ext;

    void *user_data;
};


struct _MonoMethod {
    guint16 flags;  /* method flags */
    guint16 iflags; /* method implementation flags */
    guint32 token;
    MonoClass *klass;
    MonoMethodSignature *signature;
    /* name is useful mostly for debugging */
    const char *name;
    /* this is used by the inlining algorithm */
    unsigned int inline_info:1;
    unsigned int inline_failure:1;
    unsigned int wrapper_type:5;
    unsigned int string_ctor:1;
    unsigned int save_lmf:1;
    unsigned int dynamic:1; /* created & destroyed during runtime */
    unsigned int is_generic:1; /* whenever this is a generic method definition */
    unsigned int is_inflated:1; /* whether we're a MonoMethodInflated */
    unsigned int skip_visibility:1; /* whenever to skip JIT visibility checks */
    unsigned int verification_success:1; /* whether this method has been verified successfully.*/
    /* TODO we MUST get rid of this field, it's an ugly hack nobody is proud of. */
    unsigned int is_mb_open
            : 1;        /* This is the fully open instantiation of a generic method_builder. Worse than is_tb_open, but it's temporary */
    signed int slot : 17;

    /*
     * If is_generic is TRUE, the generic_container is stored in image->property_hash,
     * using the key MONO_METHOD_PROP_GENERIC_CONTAINER.
     */
};



typedef struct {
    MonoMethod *method;
    MonoMemPool *mempool;
    MonoInst **varinfo;
    MonoMethodVar *vars;
    MonoInst *ret;
    MonoBasicBlock *bb_entry;
    MonoBasicBlock *bb_exit;
    MonoBasicBlock *bb_init;
    MonoBasicBlock **bblocks;
    MonoBasicBlock **cil_offset_to_bb;
    MonoMemPool *state_pool; /* used by instruction selection */
    MonoBasicBlock *cbb;        /* used by instruction selection */
    MonoInst *prev_ins;   /* in decompose */
    MonoJumpInfo *patch_info;
    MonoJitInfo *jit_info;
    MonoJitDynamicMethodInfo *dynamic_info;
    guint num_bblocks;
    guint locals_start;
    guint num_varinfo; /* used items in varinfo */
    guint varinfo_count; /* total storage in varinfo */
    gint stack_offset;
    gint max_ireg;
    gint cil_offset_to_bb_len;
    gint locals_min_stack_offset, locals_max_stack_offset;
    MonoRegState *rs;
    MonoSpillInfo *spill_info[16]; /* machine register spills */
    gint spill_count;
    gint spill_info_len[16];
    /* unsigned char   *cil_code; */
    MonoMethod *inlined_method; /* the method which is currently inlined */
    MonoInst *domainvar; /* a cache for the current domain */
    MonoInst *got_var; /* Global Offset Table variable */
    MonoInst **locals;
    MonoInst *rgctx_var; /* Runtime generic context variable (for static generic methods) */
    MonoInst **args;
    MonoType **arg_types;
    MonoMethod *current_method; /* The method currently processed by method_to_ir () */
    MonoMethod *method_to_register; /* The method to register in JIT info tables */
    MonoGenericContext *generic_context;

    /*
     * This variable represents the hidden argument holding the vtype
     * return address. If the method returns something other than a vtype, or
     * the vtype is returned in registers this is NULL.
     */
    MonoInst *vret_addr;

    /*
     * This is used to initialize the cil_code field of MonoInst's.
     */
    const unsigned char *ip;

    struct MonoAliasingInformation *aliasing_info;

    /* A hashtable of region ID-> SP var mappings */
    /* An SP var is a place to store the stack pointer (used by handlers)*/
    GHashTable *spvars;

    /* A hashtable of region ID -> EX var mappings */
    /* An EX var stores the exception object passed to catch/filter blocks */
    GHashTable *exvars;

    GList *ldstr_list; /* used by AOT */

    MonoDomain *domain;

    guint real_offset;
    GHashTable *cbb_hash;

    /* The current virtual register number */
    guint32 next_vreg;

    MonoGenericSharingContext *generic_sharing_context;

    unsigned char *cil_start;
    unsigned char *native_code;
    guint code_size;
    guint code_len;
    guint prolog_end;
    guint epilog_begin;
    regmask_t used_int_regs;
    guint32 opt;
    guint32 prof_options;
    guint32 flags;
    guint32 comp_done;
    guint32 verbose_level;
    guint32 stack_usage;
    guint32 param_area;
    guint32 frame_reg;
    gint32 sig_cookie;
    guint disable_aot : 1;
    guint disable_ssa : 1;
    guint disable_llvm : 1;
    guint enable_extended_bblocks : 1;
    guint run_cctors : 1;
    guint need_lmf_area : 1;
    guint compile_aot : 1;
    guint compile_llvm : 1;
    guint got_var_allocated : 1;
    guint ret_var_is_local : 1;
    guint ret_var_set : 1;
    guint globalra : 1;
    guint unverifiable : 1;
    guint skip_visibility : 1;
    guint disable_reuse_registers : 1;
    guint disable_reuse_stack_slots : 1;
    guint disable_initlocals_opt : 1;
    guint disable_omit_fp : 1;
    guint disable_vreg_to_lvreg : 1;
    guint disable_deadce_vars : 1;
    guint disable_out_of_line_bblocks : 1;
    guint extend_live_ranges : 1;
    guint has_got_slots : 1;
    guint uses_rgctx_reg : 1;
    guint uses_vtable_reg : 1;
    guint uses_simd_intrinsics : 1;
    guint keep_cil_nops : 1;
    guint gen_seq_points : 1;
    guint explicit_null_checks : 1;
    gpointer debug_info;
    guint32 lmf_offset;
    guint16 *intvars;
    MonoProfileCoverageInfo *coverage_info;
    GHashTable *token_info_hash;
    MonoCompileArch arch;
    guint32 inline_depth;
    guint32 exception_type;    /* MONO_EXCEPTION_* */
    guint32 exception_data;
    char *exception_message;
    gpointer exception_ptr;

    guint8 *encoded_unwind_ops;
    guint32 encoded_unwind_ops_len;
    GSList *unwind_ops;

    /* Fields used by the local reg allocator */
    void *reginfo;
    int reginfo_len;

    /* Maps vregs to their associated MonoInst's */
    /* vregs with an associated MonoInst are 'global' while others are 'local' */
    MonoInst **vreg_to_inst;

    /* Size of above array */
    guint32 vreg_to_inst_len;

    /*
     * The original method to compile, differs from 'method' when doing generic
     * sharing.
     */
    MonoMethod *orig_method;

    /* Patches which describe absolute addresses embedded into the native code */
    GHashTable *abs_patches;

    /* If the arch passes valuetypes by address, then for methods
       which use JMP the arch code should use these local
       variables to store the addresses of incoming valuetypes.
       The addresses should be stored in mono_arch_emit_prolog()
       and can be used when emitting code for OP_JMP.  See
       mini-ppc.c. */
    MonoInst **tailcall_valuetype_addrs;

    /* Used to implement iconv_to_r8_raw on archs that can't do raw
    copy between an ireg and a freg. This is an int32 var.*/
    MonoInst *iconv_raw_var;

    /* Used to implement fconv_to_r8_x. This is a double (8 bytes) var.*/
    MonoInst *fconv_to_r8_x_var;

    /*Use to implement simd constructors. This is a vector (16 bytes) var.*/
    MonoInst *simd_ctor_var;

    /* Used to implement dyn_call */
    MonoInst *dyn_call_var;

    /*
     * List of sequence points represented as IL offset+native offset pairs.
     * Allocated using glib.
     * IL offset can be -1 or 0xffffff to refer to the sequence points
     * inside the prolog and epilog used to implement method entry/exit events.
     */
    GPtrArray *seq_points;

    /* The encoded sequence point info */
    MonoSeqPointInfo *seq_point_info;

    /* Used by AOT */
    guint32 got_offset;
    char *asm_symbol;
} MonoCompile;


typedef struct {
    MonoObject object;
    gint32 length;
    gunichar2 chars[MONO_ZERO_LEN_ARRAY];
} MonoString;

/* This corresponds to System.Type */
typedef struct {
    MonoObject object;
    MonoType  *type;
}MonoReflectionType;

typedef struct _MonoReflectionMethod {
    MonoObject object;
    MonoMethod *method;
    MonoString *name;
    MonoReflectionType *reftype;
}MonoReflectionMethod;

typedef struct {
    MonoObject obj;
    gchar un1[0x20-8];
    MonoReflectionMethod* methoddata;
}MonoCilObject;


typedef struct {
    mono_array_size_t length;
    mono_array_lower_bound_t lower_bound;
} MonoArrayBounds;

typedef struct {
    MonoObject obj;
    /* bounds is NULL for szarrays */
    MonoArrayBounds *bounds;
    /* total number of elements of the array */
    mono_array_size_t max_length;
    /* we use double to ensure proper alignment on platforms that need it */
    double vector[MONO_ZERO_LEN_ARRAY];
} MonoArray;


typedef struct _MonoMethodDesc {
    char *snamespace;
    char *klass;
    char *name;
    char *args;
    guint num_args;
    gboolean include_namespace, klass_glob, name_glob;
} MonoMethodDesc;


typedef struct _MonoVTable {
    MonoClass *klass;
    /*
    * According to comments in gc_gcj.h, this should be the second word in
    * the vtable.
    */
    void *gc_descr;
    MonoDomain *domain;  /* each object/vtable belongs to exactly one domain */
    gpointer data; /* to store static class data */
    gpointer type; /* System.Type type for klass */
    guint8 *interface_bitmap;
    guint16 max_interface_id;
    guint8 rank;
    USE_UINT8_BIT_FIELD(guint, remote      :
        1); /* class is remotely activated */
    USE_UINT8_BIT_FIELD(guint, initialized :
        1); /* cctor has been run */
    USE_UINT8_BIT_FIELD(guint, init_failed :
        1); /* cctor execution failed */
    guint32 imt_collisions_bitmap;
    MonoRuntimeGenericContext *runtime_generic_context;
    /* do not add any fields after vtable, the structure is dynamically extended */
    gpointer vtable[MONO_ZERO_LEN_ARRAY];
} MonoVTable;

typedef enum {
    MONO_AOT_TRAMP_SPECIFIC = 0,
    MONO_AOT_TRAMP_STATIC_RGCTX = 1,
    MONO_AOT_TRAMP_IMT_THUNK = 2,
    MONO_AOT_TRAMP_NUM = 3
} MonoAotTrampoline;

typedef struct MonoAotOptions {
    char *outfile;
    gboolean save_temps;
    gboolean write_symbols;
    gboolean metadata_only;
    gboolean bind_to_runtime_version;
    gboolean full_aot;
    gboolean no_dlsym;
    gboolean static_link;
    gboolean asm_only;
    gboolean asm_writer;
    gboolean nodebug;
    gboolean soft_debug;
    int nthreads;
    int ntrampolines;
    int nrgctx_trampolines;
    int nimt_trampolines;
    gboolean print_skipped_methods;
    char *tool_prefix;
#if defined(PLATFORM_IPHONE_XCOMP)
    gboolean ficall;
#endif
    gboolean lf_eol;
} MonoAotOptions;

typedef enum {
#define PATCH_INFO(a,b) MONO_PATCH_INFO_ ## a,
#undef PATCH_INFO
    MONO_PATCH_INFO_NUM
} MonoJumpInfoType;



typedef struct MonoAotCompile {
    MonoImage *image;
    GPtrArray *methods;
    GHashTable *method_indexes;
    GHashTable *method_depth;
    MonoCompile **cfgs;
    int cfgs_size;
    GHashTable *patch_to_plt_entry;
    GHashTable *plt_offset_to_entry;
    GHashTable *patch_to_got_offset;
    GHashTable **patch_to_got_offset_by_type;
    GPtrArray *got_patches;
    GHashTable *image_hash;
    GHashTable *method_to_cfg;
    GHashTable *token_info_hash;
    GPtrArray *extra_methods;
    GPtrArray *image_table;
    GPtrArray *globals;
    GList *method_order;
    guint32 *plt_got_info_offsets;
    guint32 got_offset, plt_offset, plt_got_offset_base;
    /* Number of GOT entries reserved for trampolines */
    guint32 num_trampoline_got_entries;

    guint32 num_trampolines [MONO_AOT_TRAMP_NUM];
    guint32 trampoline_got_offset_base [MONO_AOT_TRAMP_NUM];
    guint32 trampoline_size [MONO_AOT_TRAMP_NUM];

    MonoAotOptions aot_opts;
    guint32 nmethods;
    guint32 opts;
    MonoMemPool *mempool;
    //MonoAotStats stats;
    int method_index;
    char *static_linking_symbol;
    CRITICAL_SECTION mutex;
    gboolean use_bin_writer;
    void *w;
    void *dwarf;
    void *fp;
    char *tmpfname;
    GSList *cie_program;
    GHashTable *unwind_info_offsets;
    GPtrArray *unwind_ops;
    guint32 unwind_info_offset;
    char *got_symbol;
    char *plt_symbol;
    GHashTable *method_label_hash;
    const char *temp_prefix;
    guint32 label_generator;
    MonoClass **typespec_classes;
} MonoAotCompile;

typedef struct LoadS {
    const char *s;
    size_t size;
} LoadS;


typedef MonoClass *(*mono_class_from_name)(MonoImage *image, const char *name_space,
                                           const char *name);

typedef MonoClassField *(*mono_class_get_field_from_name )(MonoClass *klass, const char *name);

typedef MonoClass *(*mono_object_get_class)(MonoObject *obj);

typedef const char *(*mono_class_get_name )(MonoClass *klass);

typedef MonoClassField *(*mono_class_get_fields )(MonoClass *klass, gpointer *iter);

typedef const char *(*mono_field_get_name )(MonoClassField *field);

typedef MonoType *(*mono_field_get_type )(MonoClassField *field);

typedef void (*mono_field_get_value )(MonoObject *obj, MonoClassField *field, void *value);

typedef MonoDomain *(*mono_domain_get )();

typedef MonoObject *
(*mono_field_get_value_object )(MonoDomain *domain, MonoClassField *field, MonoObject *obj);

typedef MonoClass *(*mono_class_get_element_class )(MonoClass *klass);

typedef MonoType *(*mono_class_get_type )(MonoClass *klass);

typedef int (*mono_type_get_type )(MonoType *type);

typedef char *(*mono_string_to_utf8)(MonoString *s);

typedef char *(*mono_array_addr_with_size )(MonoArray *array, int size, uintptr_t idx);

typedef MonoMethodDesc *(*mono_method_desc_new )(const char *name, gboolean include_namespace);

typedef MonoMethod *(*mono_method_desc_search_in_class )(MonoMethodDesc *desc, MonoClass *klass);

typedef MonoObject *(*mono_runtime_invoke )(void *method, void *obj, void **params, void **exc);

typedef void (*mono_method_desc_free )(MonoMethodDesc *desc);

typedef MonoObject *(*mono_object_new )(MonoDomain *domain, MonoClass *klass);

void mono_field_set_value(MonoObject *obj, MonoClassField *field, void *value);

uint64_t GetPlayerId();

uint64_t PlayerBase_ID(void *MonoObject);

int ShopUIPanel_CurType(void *MonoObject);

//void New_method_compile(void *pResult, MonoMethod *method);

bool mono_init_func(void *pSinfo);

extern void *g_Assembly_CSharp_Image;

#endif //END __MONO_FUNC_H
