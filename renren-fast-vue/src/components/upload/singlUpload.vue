<template>
  <div>
    <el-upload :action="resourcesUrl"
               :data="dataObj"
               list-type="picture"
               :multiple="false"
               :show-file-list="showFileList"
               :file-list="fileList"
               :before-upload="beforeUpload"
               :on-remove="handleRemove"
               :on-success="handleUploadSuccess"
               :on-preview="handlePreview">
      <el-button size="small"
                 type="primary">点击上传</el-button>
      <div slot="tip"
           class="el-upload__tip">只能上传jpg/png文件，且不超过10MB</div>
    </el-upload>
    <el-dialog :visible.sync="dialogVisible">
      <img width="100%"
           :src="fileList[0].url"
           alt="">
    </el-dialog>
  </div>
</template>
<script>
import { policy } from './policy'
import { getUUID } from '@/utils'

export default {
  name: 'singleUpload',
  props: {
    value: String
  },
  computed: {
    imageUrl () {
      return this.value
    },
    imageName () {
      if (this.value != null && this.value !== '') {
        return this.value.substr(this.value.lastIndexOf('/') + 1)
      } else {
        return null
      }
    },
    fileList () {
      return [{
        name: this.imageName,
        url: this.imageUrl
      }]
    },
    showFileList: {
      get: function () {
        return this.value != null && this.value !== '' && this.value !== undefined
      },
      set: function (newValue) {
      }
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
        // callback:'',
      },
      dialogVisible: false
    }
  },
  methods: {
    emitInput (val) {
      this.$emit('input', val)
    },
    handleRemove (file, fileList) {
      this.emitInput('')
    },
    handlePreview (file) {
      this.dialogVisible = true
    },
    beforeUpload (file) {
      // 校验类型
      let typeArray = file.type.split('/')
      if (typeArray[0] !== 'image') {
        this.message(this, '只能上传图片类型', 'info')
        return false
      }
      // 校验大小
      const isSize = file.size / (1024 * 1024) < 2
      if (!isSize) {
        this.message(this, '大小不能超过2M', 'info')
        return false
      }
      // 请求服务器获取权限
      let _self = this
      return new Promise((resolve, reject) => {
        policy().then(response => {
          console.log('响应的数据......start', response)
          _self.dataObj.policy = response.data.policy
          _self.dataObj.signature = response.data.signature
          _self.dataObj.ossaccessKeyId = response.data.accessid
          _self.dataObj.key = response.data.dir + getUUID() + file.name
          _self.dataObj.dir = response.data.dir
          _self.dataObj.host = response.data.host
          console.log('响应的数据......end', _self.dataObj)
          resolve(true)
        }).catch(err => {
          reject(err)
        })
      })
    },
    handleUploadSuccess: function (res, file) {
      console.log('上传成功......', res)
      console.log(`file`, file);
      this.showFileList = true
      this.fileList.pop()
      this.fileList.push({
        name: file.name,
        url: this.resourcesUrl + '/' + this.dataObj.key
      })
      this.emitInput(this.fileList[0].url)
      console.log(this.fileList[0].url)
    },
    message (_this, msg, type) {
      _this.$message({
        message: msg,
        type: type,
        duration: 1500
      })
    }
  }
}
</script>
<style>
</style>
