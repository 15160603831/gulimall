<!--  -->
<template>
  <div>
    <el-button type="danger"
               plain
               @click="bacthDetele">批量删除</el-button>
    <el-tree :data="menus"
             show-checkbox
             node-key="catId"
             :props="defaultProps"
             :expand-on-click-node="false"
             :default-expanded-keys="expandedkey"
             draggable
             :allow-drop="allowDrop"
             ref="treeMenu">
      <span class="custom-tree-node"
            slot-scope="{ node, data }">
        <span>{{ node.label }}</span>
        <span>
          <el-button v-if="node.level <= 2"
                     type="text"
                     size="mini"
                     @click="() => append(data)">
            新增
          </el-button>
          <el-button type="text"
                     size="mini"
                     @click="() => edit(data)">
            修改
          </el-button>
          <el-button v-if="node.childNodes.length == 0"
                     type="text"
                     size="mini"
                     @click="() => remove(node, data)">
            删除
          </el-button>
        </span>
      </span>
    </el-tree>

    <el-dialog :title="title"
               :visible.sync="dialogVisible"
               :close-on-click-modal="false">
      <el-form :model="category">
        <el-form-item label="商品分类">
          <el-input v-model="category.name"
                    autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item label="图标">
          <el-input v-model="category.icon"
                    autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item label="计量单位">
          <el-input v-model="category.productUnit"
                    autocomplete="off"></el-input>
        </el-form-item>
      </el-form>
      <span slot="footer"
            class="dialog-footer">
        <el-button @click="dialogVisible = false">取 消</el-button>
        <el-button type="primary"
                   @click="submitData">确 定</el-button>
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
  data () {
    //这里存放数据
    return {
      maxLevel: 0,
      title: "",
      dialogType: "",
      menus: [],
      category: {
        name: "",
        parentCid: 0,
        catLeve: 0,
        showStaus: 1,
        sort: 0,
        catId: null,
        icon: null,
        productUnit: null,
        productCount: null
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
    getMenus () {
      this.$http({
        url: this.$http.adornUrl("/product/pmscategory/list/tree"),
        method: "get",
      }).then(({ data }) => {
        this.menus = data.data;
      });
    },
    //拖拽时判定目标节点能否被放置
    allowDrop (draggingNode, dropNode, type) {
      // //b被拖动的节点总层数
      // console.log("draggingNode", draggingNode, dropNode, type)
      // var level =  (draggingNode);
      this.countNodeLevel(draggingNode.data);
      //当前正在拖动的节点不大于3
      console.log("深度", this.maxLevel);
      return false;
    },
    countNodeLevel (node) {
      //求出所有子节点最大深度
      if (node.children != null && node.children.length > 0) {
        for (let i = 0; i < node.children.length; i++) {
          if (node.children[i].catLevel > this.maxLevel) {
            this.maxLevel = node.catLevel[i].catLevel;
          }
          this.countNodeLevel(node.catLevel[i]);
        }
      }
    },

    submitData () {
      if (this.dialogType == "append") {
        this.addCategory();
      }
      if (this.dialogType == "edit") {
        this.editCategory();
      }
    },

    //删除
    remove (node, data) {
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
    //新增model
    append (data) {
      console.log("append", data);
      this.dialogType = "append";
      this.dialogVisible = true;
      this.title = "添加商品分类"
      this.category.parentCid = data.catId;
      this.category.catLevel = data.catLevel * 1 + 1;

      this.category.name = null;
      this.category.catId = null;
      this.category.icon = "";
      this.category.productUnit = "";
      this.category.sort = 0;
      this.category.showStaus = 1;

    },
    //添加三级分类
    addCategory () {
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
    //修改菜单名称 model
    edit (data) {
      console.log("修改啦" + data);
      this.dialogType = "edit";
      this.title = "修改商品分类"
      this.dialogVisible = true;
      //发送请求查询当前节点最新的数据
      this.$http({
        url: this.$http.adornUrl(`/product/pmscategory/info/${data.catId}`),
        method: 'get',
        params: this.$http.adornParams({})
      }).then(({ data }) => {
        this.category.name = data.data.name;
        this.category.catId = data.data.catId;
        this.category.icon = data.data.icon;
        this.category.productUnit = data.data.productUnit;
        this.category.parentCid = data.data.parentCid;
      })
    },
    //修改分类名称
    editCategory () {
      var { catId, name, icon, productUnit } = this.category;
      this.$http({
        url: this.$http.adornUrl('/product/pmscategory/update'),
        method: 'post',
        data: this.$http.adornData({ catId, name, icon, productUnit }, false)
      }).then(({ data }) => {
        this.$message({
          type: "success",
          message: "菜单修改成功!",
        });
        //关闭对话框
        this.dialogVisible = false;
        //刷新新菜单
        this.getMenus();
        //设置需要打开的菜单
        this.expandedkey = [this.category.parentCid];
      });
    },
    //批量删除
    bacthDetele () {
      let catIds = [];
      let checkNodes = this.$refs.treeMenu.getCheckedNodes();
      for (let i = 0; i < checkNodes.length; i++) {
        catIds.push(checkNodes[i].catId);
      }
      console.log("节点", catIds);
      this.$confirm(`是否批量删除${catIds}菜单, 是否继续?`, "提示", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning",
      }).then(() => {
        this.$http({
          url: this.$http.adornUrl('/product/pmscategory/delete'),
          method: 'post',
          data: this.$http.adornData(catIds, false)
        }).then(({ data }) => {
          this.$message({
            type: "success",
            message: "批量删除成功!",
          });
          //刷新新的菜单
          this.getMenus();
          //设置默认打开菜单
          // this.expandedkey = [node.parent.data.catId];
        });
      }).catch(() => {
        this.$message({
          type: "info",
          message: "已取消删除",
        });
      });

    }
  },
  //生命周期 - 创建完成（可以访问当前this实例）
  created () {
    this.getMenus();
  },
  //生命周期 - 挂载完成（可以访问DOM元素）
  mounted () { },
  beforeCreate () { }, //生命周期 - 创建之前
  beforeMount () { }, //生命周期 - 挂载之前
  beforeUpdate () { }, //生命周期 - 更新之前
  updated () { }, //生命周期 - 更新之后
  beforeDestroy () { }, //生命周期 - 销毁之前
  destroyed () { }, //生命周期 - 销毁完成
  activated () { }, //如果页面有keep-alive缓存功能，这个函数会触发
};
</script>
<style scoped>
</style>