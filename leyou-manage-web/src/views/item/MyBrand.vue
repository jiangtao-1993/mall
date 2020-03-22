<template>
    <div>

        <v-card>
            <v-card-title>
                <v-btn class="primary" @click="addBrand">新增品牌</v-btn>
                <v-spacer/>
                <v-text-field label="搜索" append-icon="search" hide-details v-model="key"></v-text-field>
            </v-card-title>


            <v-divider/>
            <v-data-table
                    :headers="headers"
                    :items="brands"
                    :pagination.sync="pagination"
                    :total-items="totalBrands"
                    :loading="loading"
                    class="elevation-1"
            >
                <template slot="items" slot-scope="props">
                    <td>{{ props.item.id }}</td>
                    <td class="text-xs-center">{{ props.item.name }}</td>
                    <td class="text-xs-center"><img :src="props.item.image"></td>
                    <td class="text-xs-center">{{ props.item.letter }}</td>
                    <td class="text-xs-center">
                        <v-btn class="warning" @click="editBrand(props.item)">编辑</v-btn>
                        <v-btn class="info" @click="deleteBrand(props.item.id)">删除</v-btn>
                    </td>
                </template>
            </v-data-table>
        </v-card>

        <!--        v-model 双向绑定，dialog，true，显示对话框-->
        <v-dialog v-model="dialog" width="500" persistent>
            <brand-form @close="closeWindow" :is-edit="isEdit" :old-brand="oldBrand"></brand-form>
        </v-dialog>


    </div>
</template>

<script>
    //倒入组件
    import BrandForm from "./BrandForm"

    export default {
        components: {
            BrandForm //组件的声明
        },
        name: "my-brand",
        data() {
            return {
                oldBrand: {},
                isEdit: false,
                dialog: false,
                key: "",
                totalBrands: 0, // 总条数
                brands: [], // 当前页品牌数据
                loading: true, // 是否在加载中
                pagination: {}, // 分页信息
                headers: [
                    {text: 'id', align: 'center', value: 'id'},
                    {text: '名称', align: 'center', sortable: false, value: 'name'},
                    {text: 'LOGO', align: 'center', sortable: false, value: 'image'},
                    {text: '首字母', align: 'center', value: 'letter', sortable: true},
                    {text: '操作', align: 'center', sortable: false}
                ]
            }
        },
        mounted() { // 渲染后执行
            // 查询数据
            this.getDataFromServer();
        },
        methods: {
            getDataFromServer() { // 从服务的加载数据的方法。
                this.$http.get("/item/brand/page", {
                    params: {
                        page: this.pagination.page, //当前页
                        rows: this.pagination.rowsPerPage, //页容量
                        sortBy: this.pagination.sortBy, //根据什么排序
                        desc: this.pagination.descending, //排序的条件
                        key: this.key //请求发出时，携带key
                    }
                }).then(resp => {
                    this.totalBrands = resp.data.total,// 总条数
                        this.brands = resp.data.items, // 当前页品牌数据
                        this.loading = false // 是否在加载中
                }).catch(resp => {
                    this.$message.error("品牌查询请求失败");
                })
            },
            addBrand() {
                this.oldBrand = {
                    image: ""
                };
                this.isEdit = false;//新增isEdit为false
                this.dialog = true;
            },
            closeWindow() {
                this.dialog = false;
            },
            editBrand(oldBrand) {
                //根据品牌的id查询对应的分类信息
                this.$http.get("/item/category/of/brand/" + oldBrand.id)
                    .then(resp => {
                        //分类的数组，值从返回的data中获取（分类的集合）
                        oldBrand.categories = resp.data;
                        this.oldBrand = oldBrand;
                        this.isEdit = true;//修改是isEdit为true
                        this.dialog = true;
                    }).catch(resp => {
                    this.$message.error("根据品牌查询分类失败");
                })
            },
            deleteBrand(brandId){
                this.$message.confirm("您确定要删除")
                    .then(resp=>{
                        this.$http.delete("item/brand/"+brandId)
                    }).catch(resp=>{

                })
            }
        },
        watch: {
            pagination: {
                deep: true, //深度监控，表示监控对象本身，以及下面所有的属性
                handler() { //表示有变化后触发的函数
                    //处理分页页容量的bug,如果当前rows为-1表示，要展示所有，展示所有，就把所有的条数赋值给页容量
                    if (this.pagination.rowsPerPage === -1) {
                        this.pagination.rowsPerPage = this.totalBrands;
                    }
                    this.getDataFromServer();
                }
            },
            key() {
                this.getDataFromServer();
            }
        }
    }
</script>