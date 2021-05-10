<template>
  <div>
    <el-upload :action="resourcesUrl"
               :data="dataObj"
               :list-type="listType"
               :file-list="fileList"
               :before-upload="beforeUpload"
               :on-remove="handleRemove"
               :on-success="handleUploadSuccess"
               :on-preview="handlePreview"
               :limit="maxCount"
               :on-exceed="handleExceed"
               :show-file-list="showFile">
      <i class="el-icon-plus"></i>
    </el-upload>
    <el-dialog :visible.sync="dialogVisible">
      <img width="100%"
           :src="resourcesUrl + dialogImageUrl"
           alt />
    </el-dialog>
  </div>
</template>
<script>
import { policy } from './policy'
import { getUUID } from '@/utils'

export default {
  name: 'multiUpload',
  props: {
    // 图片属性数组
    value: Array,
    // 最大上传图片数量
    maxCount: {
      type: Number,
      default: 30
    },
    listType: {
      type: String,
      default: 'picture-card'
    },
    showFile: {
      type: Boolean,
      default: true
    }

  },
  data () {
    return {
      resourcesUrl: window.SITE_CONFIG.resourcesUrl,
      dataObj: {
        policy: '',
        signature: '',
        key: '',
        ossaccessKeyId: '',
        dir: '',
        host: '',
        uuid: ''
      },
      dialogVisible: false,
      dialogImageUrl: null
    }
  },
  computed: {
    fileList () {
      let fileList = []
      for (let i = 0; i < this.value.length; i++) {
        fileList.push({ url: this.resourcesUrl + this.value[i] })
      }
      return fileList
    }
  },
  methods: {
    emitInput (fileList) {
      let value = []
      for (let i = 0; i < fileList.length; i++) {
        let saveUrl = fileList[i].url.replace(this.dataObj.host + '/', '')
        value.push(saveUrl)
      }
      this.$emit('input', value)
    },
    handleRemove (file, fileList) {
      this.emitInput(fileList)
    },
    // 点击放大之前
    handlePreview (file) {
      this.dialogVisible = true
      this.dialogImageUrl = file.url
    },
    beforeUpload (file) {
      let _self = this
      return new Promise((resolve, reject) => {
        policy()
          .then(response => {
            console.log('图片名：', file.name)
            _self.dataObj.policy = response.data.policy
            _self.dataObj.signature = response.data.signature
            _self.dataObj.ossaccessKeyId = response.data.accessid
            _self.dataObj.key = response.data.dir + getUUID() + file.name
            _self.dataObj.dir = response.data.dir
            _self.dataObj.host = response.data.host
            resolve(true)
          })
          .catch(err => {
            console.log('出错了...', err)
            reject(err)
          })
      })
    },
    handleUploadSuccess (res, file) {
      this.fileList.push({
        name: file.name,
        url: this.dataObj.host + '/' + this.dataObj.key
      })
      this.emitInput(this.fileList)
    },
    handleExceed (files, fileList) {
      this.$message({
        message: '最多只能上传' + this.maxCount + '张图片',
        type: 'warning',
        duration: 1000
      })
    }
  }
}
</script>
<style>
</style>
