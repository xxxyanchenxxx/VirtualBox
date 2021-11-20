#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <elf.h>
#include <errno.h>

#include "Symbol.h"
#include "Log.h"

#include <Jni/Helper.h>
#include <malloc.h>
#include <fcntl.h>
#include <sys/mman.h>

#define MAX_NAME_LEN 256
#define Max_MAPS 1000
#define MEMORY_ONLY  "[memory]"
struct mm {
    char name[MAX_NAME_LEN];
    unsigned long start, end;
};
typedef struct symtab *symtab_t;
struct symlist {
    Elf32_Sym *sym; /* symbols */
    char *str; /* symbol strings */
    unsigned num; /* number of symbols */
};
struct symtab {
    struct symlist *st; /* "static" symbols */
    struct symlist *dyn; /* dynamic symbols */
};


static ssize_t read_strtab(FILE *fp, elf_shdr *shdr, char **datap) {
    elf_word sh_size;
    long cur_off;
    char *data;


    sh_size = shdr->sh_size;

    if ((size_t) sh_size > SIZE_MAX - 1) {
        fprintf(stderr, "read_strtab: %s", strerror(EFBIG));
        goto _ret;
    }


    cur_off = ftell(fp);

    if (fseek(fp, shdr->sh_offset, SEEK_SET) != 0) {
        perror("read_strtab: fseek");
        goto _ret;
    }

    if ((data = (char *) malloc(sh_size + 1)) == NULL) {
        perror("read_strtab: malloc");
        goto _ret;
    }

    if (fread(data, 1, sh_size, fp) != sh_size) {
        perror("read_strtab: fread");
        goto _free;
    }

    data[sh_size] = 0;

    if (fseek(fp, cur_off, SEEK_SET) != 0) {
        perror("read_strtab: fseek");
        goto _free;
    }

    *datap = data;

    return (ssize_t) sh_size;

    _free:
    free(data);

    _ret:
    return -1;
}


static int resolve_symbol_from_symtab(FILE *fp, elf_shdr *symtab, char *strtab,
                                      size_t strtab_size, const char *symname, intptr_t *symval) {
    elf_word i, num_syms;
    elf_sym sym;
    long cur_off;

    int r = -1;

    cur_off = ftell(fp);

    if (fseek(fp, symtab->sh_offset, SEEK_SET) != 0) {
        perror("resolve_symbol_from_symtab: fseek");
        goto _ret;
    }

    num_syms = symtab->sh_size / sizeof(elf_sym);

    for (i = 0; i < num_syms; i++) {
        if (fread(&sym, sizeof(elf_sym), 1, fp) != 1) {
            perror("resolve_symbol_from_symtab: fread");
            goto _ret;
        }

        if (sym.st_name < strtab_size &&
            strcmp(&strtab[sym.st_name], symname) == 0) {
            *symval = sym.st_value;
            break;
        }
    }

    if (fseek(fp, cur_off, SEEK_SET) != 0) {
        perror("resolve_symbol_from_symtab: fseek");
        goto _ret;
    }

    if (i < num_syms)
        r = 0;

    _ret:
    return r;
}


static int resolve_symbol_from_sections(FILE *fp, elf_shdr *shdrs,
                                        elf_half num_sects, const char *symname, intptr_t *symval) {
    elf_half i;
    elf_shdr *shdr, *strtab_shdr;
    char *strtab;
    ssize_t strtab_size;

    int r = -1;

    for (i = 0; i < num_sects; i++) {
        shdr = &shdrs[i];

        if (shdr->sh_type == SHT_SYMTAB && shdr->sh_link < num_sects) {
            strtab_shdr = &shdrs[shdr->sh_link];

            if ((strtab_size = read_strtab(fp, strtab_shdr, &strtab)) < 0)
                goto _ret;

            r = resolve_symbol_from_symtab(fp, shdr, strtab, (size_t) strtab_size,
                                           symname, symval);

            free(strtab);

            if (r == 0)
                goto _ret;
        }

    }

    _ret:
    return r;
}


/* Resolve symbol named `symname' from ELF file `filename' and return the symbol's
 * value in `*symval'. Returns 0 on success, or -1 on failure. The following code
 * makes so many assumptions that listing them all here is pointless; we just
 * needed to make it as straightforward and minimal as possible.
 */
int resolve_symbol(const char *filename, const char *symname, intptr_t *symval) {
    FILE *fp;
    elf_ehdr ehdr;
    elf_shdr *shdrs;
    elf_half shnum;

    int r = -1;

    if ((fp = fopen(filename, "r")) == NULL) {
        perror("resolve_symbol: fopen");
        goto _ret;
    }

    if (fread(&ehdr, sizeof(ehdr), 1, fp) != 1) {
        perror("resolve_symbol: fread");
        goto _close;
    }

    if (fseek(fp, ehdr.e_shoff, SEEK_SET) != 0) {
        perror("resolve_symbol: fseek");
        goto _close;
    }

    shnum = ehdr.e_shnum;

    if ((shdrs = (elf_shdr *) (calloc(shnum, sizeof(elf_shdr)))) == NULL) {
        perror("resolve_symbol: calloc");
        goto _close;
    }

    if (fread(shdrs, sizeof(elf_shdr), shnum, fp) != shnum) {
        perror("resolve_symbol: fread");
        goto _free;
    }

    r = resolve_symbol_from_sections(fp, shdrs, shnum, symname, symval);

    _free:
    free(shdrs);

    _close:
    fclose(fp);

    _ret:
    return r;
}

intptr_t get_addr(const char *name) {
    char buf[BUFSIZ], *tok[6];
    int i;
    FILE *fp;

    intptr_t r = NULL;

    snprintf(buf, sizeof(buf), "/proc/self/maps");

    if ((fp = fopen(buf, "r")) == NULL) {
        perror("get_linker_addr: fopen");
        goto ret;
    }

    while (fgets(buf, sizeof(buf), fp)) {
        i = strlen(buf);
        if (i > 0 && buf[i - 1] == '\n')
            buf[i - 1] = 0;

        tok[0] = strtok(buf, " ");
        for (i = 1; i < 6; i++)
            tok[i] = strtok(NULL, " ");

        if (tok[5] && strcmp(tok[5], name) == 0) {
            r = (intptr_t) strtoul(tok[0], NULL, 16);
            goto close;
        }
    }

    close:
    fclose(fp);

    ret:
    return r;
}

////////////////////////////////////////////////////////////////////////////////////////////////////

static void* xmalloc(size_t size) {
    void *p;
    p = malloc(size);
    if (!p) {
        printf("Out of memory\n");
        exit(1);
    }
    return p;
}

static int my_pread(int fd, void *buf, size_t count, off_t offset) {
    lseek(fd, offset, SEEK_SET);
    return read(fd, buf, count);
}

static struct symlist* get_syms(int fd, Elf32_Shdr *symh, Elf32_Shdr *strh) {
    struct symlist *sl, *ret;
    int rv;

    ret = NULL;
    sl = (struct symlist *) xmalloc(sizeof(struct symlist));
    sl->str = NULL;
    sl->sym = NULL;

    /* sanity */
    if (symh->sh_size % sizeof(Elf32_Sym)) {
        //printf("elf_error\n");
        goto out;
    }

    /* symbol table */
    sl->num = symh->sh_size / sizeof(Elf32_Sym);
    sl->sym = (Elf32_Sym *) xmalloc(symh->sh_size);
    rv = my_pread(fd, sl->sym, symh->sh_size, symh->sh_offset);
    if (0 > rv) {
        //perror("read");
        goto out;
    }
    if (rv != symh->sh_size) {
        //printf("elf error\n");
        goto out;
    }

    /* string table */
    sl->str = (char *) xmalloc(strh->sh_size);
    rv = my_pread(fd, sl->str, strh->sh_size, strh->sh_offset);
    if (0 > rv) {
        //perror("read");
        goto out;
    }
    if (rv != strh->sh_size) {
        //printf("elf error");
        goto out;
    }

    ret = sl;
    out: return ret;
}

static int do_load(int fd, symtab_t symtab) {
    int rv;
    size_t size;
    Elf32_Ehdr ehdr;
    Elf32_Shdr *shdr = NULL, *p;
    Elf32_Shdr *dynsymh, *dynstrh;
    Elf32_Shdr *symh, *strh;
    char *shstrtab = NULL;
    int i;
    int ret = -1;

    /* elf header */
    rv = read(fd, &ehdr, sizeof(ehdr));
    if (0 > rv) {
        ALOGD("read\n");
        goto out;
    }
    if (rv != sizeof(ehdr)) {
        ALOGD("elf error 1\n");
        goto out;
    }
    if (strncmp((const char *) ELFMAG, (const char *) ehdr.e_ident, SELFMAG)) { /* sanity */
        ALOGD("not an elf\n");
        goto out;
    }
    if (sizeof(Elf32_Shdr) != ehdr.e_shentsize) { /* sanity */
        ALOGD("elf error 2\n");
        goto out;
    }

    /* section header table */
    size = ehdr.e_shentsize * ehdr.e_shnum;
    shdr = (Elf32_Shdr *) xmalloc(size);
    rv = my_pread(fd, shdr, size, ehdr.e_shoff);
    if (0 > rv) {
        ALOGD("read\n");
        goto out;
    }
    if (rv != size) {
        ALOGD("elf error 3 %d %d\n", rv, size);
        goto out;
    }

    /* section header string table */
    size = shdr[ehdr.e_shstrndx].sh_size;
    shstrtab = (char *) xmalloc(size);
    rv = my_pread(fd, shstrtab, size, shdr[ehdr.e_shstrndx].sh_offset);
    if (0 > rv) {
        ALOGD("read\n");
        goto out;
    }
    if (rv != size) {
        ALOGD("elf error 4 %d %d\n", rv, size);
        goto out;
    }

    /* symbol table headers */
    symh = dynsymh = NULL;
    strh = dynstrh = NULL;
    for (i = 0, p = shdr; i < ehdr.e_shnum; i++, p++)
        if (SHT_SYMTAB == p->sh_type) {
            if (symh) {
                ALOGD("too many symbol tables\n");
                goto out;
            }
            symh = p;
        } else if (SHT_DYNSYM == p->sh_type) {
            if (dynsymh) {
                ALOGD("too many symbol tables\n");
                goto out;
            }
            dynsymh = p;
        } else if (SHT_STRTAB == p->sh_type
                   && !strncmp(shstrtab + p->sh_name, ".strtab", 7)) {
            if (strh) {
                ALOGD("too many string tables\n");
                goto out;
            }
            strh = p;
        } else if (SHT_STRTAB == p->sh_type
                   && !strncmp(shstrtab + p->sh_name, ".dynstr", 7)) {
            if (dynstrh) {
                ALOGD("too many string tables\n");
                goto out;
            }
            dynstrh = p;
        }
    /* sanity checks */
    if ((!dynsymh && dynstrh) || (dynsymh && !dynstrh)) {
        ALOGD("bad dynamic symbol table\n");
        goto out;
    }
    if ((!symh && strh) || (symh && !strh)) {
        ALOGD("bad symbol table\n");
        goto out;
    }
    if (!dynsymh && !symh) {
        ALOGD("no symbol table\n");
        goto out;
    }

    /* symbol tables */
    if (dynsymh)
        symtab->dyn = get_syms(fd, dynsymh, dynstrh);
    if (symh)
        symtab->st = get_syms(fd, symh, strh);
    ret = 0;
    out: free(shstrtab);
    free(shdr);
    return ret;
}

static symtab_t load_symtab(char *filename) {
    int fd;
    symtab_t symtab;

    symtab = (symtab_t) xmalloc(sizeof(*symtab));
    memset(symtab, 0, sizeof(*symtab));

    fd = open(filename, O_RDONLY);
    if (0 > fd) {
        ALOGE("%s open\n", __func__);
        return NULL;
    }
    if (0 > do_load(fd, symtab)) {
        ALOGE("Error ELF parsing %s\n", filename);
        free(symtab);
        symtab = NULL;
    }
    close(fd);
    return symtab;
}

int readln(int fd, char *p_buf) {
    char *pStart = p_buf;
    while (1) {
        int rv = read(fd, pStart++, sizeof(char));
        if (rv != 1) {
            return 0;
        }

        if (*(pStart - 1) == 0xa) {
            *(pStart - 1) = 0;
            return pStart - p_buf;
        }
    }

    return pStart - p_buf;
}

static int load_memmap(struct mm *mm, int *nmmp, bool bmem = true) {
    size_t buf_size = 0x1000;
    char *p_buf = (char *) malloc(buf_size);
    char name[MAX_NAME_LEN] = {0};
    char *p;
    unsigned long start, end;
    struct mm *m;
    int nmm = 0;
    int rv;
    int i;

    sprintf(p_buf, "/proc/%u/maps", getpid());
    int fd = open(p_buf, O_RDONLY, 0);
    if (0 > fd) {
        ALOGE("Can't open %s for reading, error:%x", p_buf, errno);
        strcpy(p_buf, "/proc/self/maps");
        fd = open(p_buf, O_RDONLY, 0);
        if (0 > fd) {
            ALOGE("Can't open %s for reading, error:%x", p_buf, errno);
            free(p_buf);
            return -1;
        }
    }

    while (nmm < *nmmp) {
        int nlen = readln(fd, p_buf);
        if (nlen < 1) {
            break;
        }

        rv = sscanf(p_buf, "%08lx-%08lx %*s %*s %*s %*s %s\n", &start, &end, name);
        if (strstr(name, "[stack:")) {
            continue;
        }

        if (bmem)
            if (rv == 2) {
                m = &mm[nmm++];
                m->start = start;
                m->end = end;
                memcpy(m->name, MEMORY_ONLY, sizeof(MEMORY_ONLY));
                continue;
            }

        /* search backward for other mapping with same name */
        for (i = nmm - 1; i >= 0; i--) {
            m = &mm[i];
            if (!strcmp(m->name, name))
                break;
        }

        if (i >= 0) {
            if (start < m->start)
                m->start = start;
            if (end > m->end)
                m->end = end;
        } else {
            /* new entry */
            m = &mm[nmm++];
            m->start = start;
            m->end = end;
            strcpy(m->name, name);
        }
    }

    *nmmp = nmm;
    free(p_buf);
    return 0;
}

/* Find libc in MM, storing no more than LEN-1 chars of
 its name in NAME and set START to its starting
 address.  If libc cannot be found return -1 and
 leave NAME and START untouched.  Otherwise return 0
 and null-terminated NAME. */
static int find_libname(const char *libn, char *name, int len, unsigned long *start,
                        struct mm *mm, int nmm) {
    int i;
    struct mm *m;
    char *p;
    for (i = 0, m = mm; i < nmm; i++, m++) {
        if (!strcmp(m->name, MEMORY_ONLY))
            continue;
        p = strrchr(m->name, '/');
        if (!p)
            continue;
        p++;
        if (strncmp(libn, p, strlen(libn)))
            continue;
        p += strlen(libn);

        /* here comes our crude test -> 'libc.so' or 'libc-[0-9]' */
        if (!strncmp("so", p, 2) || 1) // || (p[0] == '-' && isdigit(p[1])))
            break;
    }
    if (i >= nmm)
        /* not found */
        return -1;

    *start = m->start;
    strncpy(name, m->name, len);
    if (strlen(m->name) >= len)
        name[len - 1] = '\0';

    mprotect((void*) m->start, m->end - m->start,
             PROT_READ | PROT_WRITE | PROT_EXEC);
    return 0;
}

static int lookup2(struct symlist *sl, unsigned char type, char *name,
                   unsigned long *val) {
    Elf32_Sym *p;
    int len;
    int i;

    len = strlen(name);
    for (i = 0, p = sl->sym; i < sl->num; i++, p++) {
        //ALOGD("name: %s %x\n", sl->str+p->st_name, p->st_value)
        if (!strncmp(sl->str + p->st_name, name, len)
            && *(sl->str + p->st_name + len) == 0
            && ELF32_ST_TYPE(p->st_info) == type) {
            //if (p->st_value != 0) {
            *val = p->st_value;
            return 0;
            //}
        }
    }
    return -1;
}

static int lookup_sym(symtab_t s, unsigned char type, char *name,
                      unsigned long *val) {
    if (s->dyn && !lookup2(s->dyn, type, name, val))
        return 0;
    if (s->st && !lookup2(s->st, type, name, val))
        return 0;
    return -1;
}

static int lookup_func_sym(symtab_t s, char *name, unsigned long *val) {
    return lookup_sym(s, STT_FUNC, name, val);
}

int findSymbol(const char *name, const char *libn, unsigned long *addr) {
    return find_name(name, libn, addr, NULL);
}

int findBase(const char *name, unsigned long *addr) {
    return find_libbase(name, addr, NULL);
}

int find_name(const char *name, const char *libn, unsigned long *addr, char *fullname) {
    int nmm = Max_MAPS;
    struct mm mm[Max_MAPS] = {0};
    unsigned long libcaddr;
    symtab_t s;
    char libc[PATH_MAX] = {0};
    if (!fullname) {
        fullname = libc;
    }

    if (0 > load_memmap(mm, &nmm, false)) {
        ALOGD("cannot read memory map.");
        return -1;
    }
    if (0 > find_libname((char *) libn, (char *) fullname, PATH_MAX, &libcaddr, mm, nmm)) {
        ALOGD("cannot find lib: %s.", libn);
        return -1;
    }
    s = load_symtab(fullname);
    if (!s) {
        ALOGD("cannot read symbol table lib: %s.", libn);
        return -1;
    }
    if (0 > lookup_func_sym(s, (char *) name, addr)) {
        //ALOGD("cannot find function: %s.", name);
        return -1;
    }
    *addr += libcaddr;
    return 0;
}

int find_libbase(const char *libn, unsigned long *addr, char *fullname) {
    int nmm = Max_MAPS;
    struct mm mm[Max_MAPS] = {0};
    unsigned long libcaddr = 0;
    symtab_t s;

    if (0 > load_memmap(mm, &nmm, false)) {
        ALOGD("cannot read memory map\n");
        return -1;
    }
    if (0 > find_libname(libn, fullname, PATH_MAX, &libcaddr, mm, nmm)) {
        ALOGD("cannot find lib: %s\n", libn);
        return -1;
    }
    if (addr) {
        *addr = libcaddr;
    }
    return EXIT_SUCCESS;
}