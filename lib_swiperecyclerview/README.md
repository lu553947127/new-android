修复setHasStableIds无效的问题，
通过setHasStableIds和getItemId解决头像闪烁问题，
但库里的AdapterWrapper无视了封装的adapter的hasStableIds配置，
所以在AdapterWrapper.init添加了一行，
setHasStableIds(adapter.hasStableIds());
