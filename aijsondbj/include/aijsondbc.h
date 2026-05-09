#ifndef AIJSONDBC_H
#def  AIJSONDBC_H
int ffi_aijsondb_load_data(const char* filename, const char* schema);
int ffi_aijsondb_query(const char* query, char* result_buffer, int buffer_size);
int ffi_aijsondb_free_data();
int ffi_aijsondb_last_error(char* result_buffer, int buffer_size);
#endif