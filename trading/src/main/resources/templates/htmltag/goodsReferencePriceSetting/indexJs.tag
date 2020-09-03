<script>
    /*********************变量定义区 begin*************/
        //行索引计数器
        //如 let itemIndex = 0;
    let _grid = $('#grid');
    let _form = $('#_form');
    var dia;
    /*********************变量定义区 end***************/


    /******************************驱动执行区 begin***************************/
    $(function(){

        var requestUrl = "${ctxPath}/goodsReferencePriceSetting/getAllGoods.action";

        var setting  = {
            view: {
                //addHoverDom:addHoverDom,
                //removeHoverDom:removeHoverDom,
                selectedMulti: false,
                showIcon:false
            },
            edit: {
                enable: true,
                showRenameBtn: false,
                showRemoveBtn: false
            },
            data : {
                key:{
                    name:"name"
                },
                simpleData : {
                    enable : true,
                    idKey  : "id",
                    pIdKey : "parent"
                }
            },
            callback: {
                onClick: zTreeOnClick
            }
        };

        $.ajax({
            url : requestUrl,
            dataType : 'json',
            type : 'post',
            async : false,
            success : function(data) {
                var treeObj =  $.fn.zTree.init($("#goodsTree"), setting , data);
                treeObj.expandAll(false);
            }
        });

        function filter(treeId, parentNode, childNodes) {
            if (!childNodes.data) return null;
            for (var i=0, l=childNodes.data.length; i<l; i++) {
                childNodes.data[i].name = childNodes.data[i].name.replace('','');
            }
            return childNodes.data;
        };
    });


    /******************************驱动执行区 end****************************/

    /*****************************************函数区 begin************************************/
    function zTreeOnClick(event, menuTree, treeNode)
    {
        document.getElementById("tableDiv").style.display = "block";
        var oid = treeNode.id;
        $("input[name='parentGoodsId']").val(oid);
        $(window).resize(function () {
            _grid.bootstrapTable('resetView')
        });
        _grid.bootstrapTable('refreshOptions', {
            url: '/goodsReferencePriceSetting/getGoodsByParentId.action',
            columns: [
                {
                    field: 'goodsName',
                    title: '品类名称 ',
                    align: 'center'
                },   {
                    field: 'referenceRule',
                    title: '参考价规则',
                    align: 'center'
                },{
                    field: 'oid',
                    title: '操作',
                    align: 'center',
                    formatter: function (value, row, index) {
                        var actions = [];
                        actions.push('<a href="#" onclick="edit(\'' + row.goodsId + '\',\'' + row.goodsName + '\',\'' + row.parentGoodsId + '\',\'' + row.referenceRule + '\')"><i class="fa fa-edit"></i>编辑</a> ');
                        return actions.join('');
                    }
                }
            ],
        });
    }

    /**
     * 打开新增/修改窗口
     */
    function edit(goodsId, goodsName, parentGoodsId, referenceRule) {
        if(referenceRule == "undefined" || referenceRule == "" || referenceRule == "规则1")
        {
            referenceRule = 1;
        }
        else if(referenceRule == "规则2")
        {
            referenceRule = 2;
        }
        else if(referenceRule == "规则3")
        {
            referenceRule = 3;
        }
        else if(referenceRule == "无")
        {
            referenceRule = 4;
        }
        dia = bs4pop.dialog({
            title: '新增价格预警',//对话框title
            content: '${contextPath}/goodsReferencePriceSetting/add.html?goodsId=' + goodsId + '&goodsName=' + goodsName + '&parentGoodsId='+ parentGoodsId +'&referenceRule=' + referenceRule, //对话框内容，可以是 string、element，$object
            width: '40%',//宽度
            height: '85%',//高度
            isIframe: true,//默认是页面层，非iframe
            btns: [
                {
                    label: '确定', className: 'btn btn-primary', onClick(e, $iframe) {
                        let diaWindow = $iframe[0].contentWindow;
                        bui.util.debounce(diaWindow.saveOrUpdateHandler, 1000, true)()
                        return false;
                    }
                }, {
                    label: '取消', className: 'btn btn-secondary', onClick(e, $iframe) {

                    }
                }]
        });

    }

    /**
     * table参数组装
     * 可修改queryParams向服务器发送其余的参数
     * @param params
     */
    function queryParams(params) {
        let temp = {
            rows: params.limit,   //页面大小
            page: ((params.offset / params.limit) + 1) || 1, //页码
            sort: params.sort,
            order: params.order
        };
        return $.extend(temp, bui.util.bindGridMeta2Form('grid', 'queryForm'));
    }

    /**
     * 查询处理(根据快捷吗查询商品)
     */
    function queryDataHandler() {
        document.getElementById("tableDiv").style.display = "block";
        $(window).resize(function () {
            _grid.bootstrapTable('resetView')
        });
        _grid.bootstrapTable('refreshOptions', {
            url: '/goodsReferencePriceSetting/getGoodsByKeyword.action',
            columns: [
                {
                    field: 'goodsName',
                    title: '品类名称 ',
                    align: 'center'
                },   {
                    field: 'referenceRule',
                    title: '参考价规则',
                    align: 'center'
                },{
                    field: 'oid',
                    title: '操作',
                    align: 'center',
                    formatter: function (value, row, index) {
                        var actions = [];
                        actions.push('<a href="#" onclick="edit(\'' + row.goodsId + '\',\'' + row.goodsName + '\',\'' + row.parentGoodsId + '\',\'' + row.referenceRule + '\')"><i class="fa fa-edit"></i>编辑</a> ');
                        return actions.join('');
                    }
                }
            ],
        });
        _grid.bootstrapTable('refresh');
    }
    /*****************************************函数区 end**************************************/

</script>