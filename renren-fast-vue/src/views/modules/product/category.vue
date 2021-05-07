<!--  -->
<template>
  <div>
    <el-tree :data="menus"
             show-checkbox
             node-key="catId"
             :props="defaultProps"
             :expand-on-click-node="false"
             :default-expanded-keys="expandedkey">
      <span class="custom-tree-node"
            slot-scope="{ node, data }">
        <span>{{ node.label }}</span>
        <span>
          <el-button
            v-if="node.level <= 2"
            type="text"
            size="mini"
            @click="() => append(data)"
          >
            新增
          </el-button>
          <el-button type="text" size="mini" @click="() => edit(data)">
            修改
          </el-button>
          <el-button
            v-if="node.childNodes.length == 0"
            type="text"
            size="mini"
            @click="() => remove(node, data)"
          >
            删除
          </el-button>
        </span>
      </span>
    </el-tree>

    <el-dialog :dialogName="提示" :visible.sync="dialogVisible">
      <el-form :model="category">
        <el-form-item label="商品分类">
          <el-input v-model="category.name" autocomplete="off"></el-input>
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="submitData">确 定</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
//这里可以导入其他文件（比如：组件，工具js，第三方插件js，json文件，图片文件等等）
//例如：import 《组件名称》 from '《组件路径》';

export default {
  //import引入的组件需要注入到对象中才能使用
  components: { name: "" },
  props: {},
  data() {
    //这里存放数据
    return {
      dialogName:"提示",
      dialogType: "",
      menus: [],
      category: {
        name: "",
        parentCid: 0,
        catLeve: 0,
        showStaus: 1,
        sort: 0,
        catId: null,
      },
      dialogVisible: false,
      expandedkey: [],
      defaultProps: {
        children: "children",
        label: "name",
      },
    };
  },
  //监听属性 类似于data概念
  computed: {},
  //监控data中的数据变化
  watch: {},
  //方法集合
  methods: {
    //获取商品菜单
    getMenus() {
      this.$http({
        url: this.$http.adornUrl("/product/pmscategory/list/tree"),
        method: "get",
      }).then(({ data }) => {
        this.menus = data.data;
      });
    },

    submitData() {
      if(this.dialogType=="append"){
        this.addCategory();
      }
      if(this.dialogType=="edit"){
        this.editCategory();
      }
    },

    //删除
    remove(node, data) {
      console.log("remove", node, data);
      this.$confirm(`此操作将永久删除【${data.name}】菜单, 是否继续?`, "提示", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning",
      })
        .then(() => {
          var ids = [data.catId];
          this.$http({
            url: this.$http.adornUrl("/product/pmscategory/delete"),
            method: "post",
            data: this.$http.adornData(ids, false),
          }).then(({ data }) => {
            this.$message({
              type: "success",
              message: "菜单删除成功!",
            });
            //刷新新的菜单
            this.getMenus();
            //设置默认打开菜单
            this.expandedkey = [node.parent.data.catId];
          });
        })
        .catch(() => {
          this.$message({
            type: "info",
            message: "已取消删除",
          });
        });
    },
    append(data) {
      console.log("append", data);
      this.dialogType = "append";
      this.dialogVisible = true;
      this.dialogName="添加商品分类"
      this.category.parentCid = data.catId;
      this.category.catLevel = data.catLevel * 1 + 1;
    },
    //添加三级分类
    addCategory() {
      console.log("添加三级分类" + this.category);
      this.$http({
        url: this.$http.adornUrl("/product/pmscategory/save"),
        method: "post",
        data: this.$http.adornData(this.category, false),
      }).then(({ data }) => {
        this.$message({
          type: "success",
          message: "菜单保存成功!",
        });
        //关闭对话框
        this.dialogVisible = false;
        //刷新新菜单
        this.getMenus();
        //设置需要打开的菜单
        this.expandedkey = [this.category.parentCid];
      });
    },
    //修改菜单名称
    edit(data) {
      console.log("修改啦" + data);
      this.dialogType = "edit";
      this.dialogName="修改商品分类"
      this.dialogVisible = true;
      this.category.name = data.name;
      this.category.catId = data.catId;
    },
    editCategory(){
      this.$http({
      url: this.$http.adornUrl(''),
      method: 'post',
      data: this.$http.adornData(data, false)
      }).then(({ data }) => { });
    }
  },
  //生命周期 - 创建完成（可以访问当前this实例）
  created() {
    this.getMenus();
  },
  //生命周期 - 挂载完成（可以访问DOM元素）
  mounted() {},
  beforeCreate() {}, //生命周期 - 创建之前
  beforeMount() {}, //生命周期 - 挂载之前
  beforeUpdate() {}, //生命周期 - 更新之前
  updated() {}, //生命周期 - 更新之后
  beforeDestroy() {}, //生命周期 - 销毁之前
  destroyed() {}, //生命周期 - 销毁完成
  activated() {}, //如果页面有keep-alive缓存功能，这个函数会触发
};
</script>
<style scoped>
</style>
