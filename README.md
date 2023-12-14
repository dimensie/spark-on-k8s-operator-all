# spark operator部署

1. 从官网在线安装operator

```bash
helm repo add spark-operator https://googlecloudplatform.github.io/spark-on-k8s-operator

helm install spark-operator-demo spark-operator/spark-operator --namespace spark-operator-ns --create-namespace

helm status --namespace spark-operator-ns spark-operator-demo
```

2. 打包spark镜像，或者直接使用官方镜像,从官网下载对应版本的spark客户端

```bash
cd $SPARK_HOME
# 构建镜像
$SPARK_HOME/bin/docker-image-tool.sh -r spark-operator-client/spark -t hadoop3.0.0 build  build

```

3. 创建clusterrole，主要是为了添加crd的权限，这里我们主要是用了sparkapplication和scheduledsparkapplications

```yaml
apiVersion: rbac.authorization.k8s.io/v1
kind: ClusterRole
metadata:
  name: spark-operator-role
rules:
- apiGroups:
  - sparkoperator.k8s.io
resources:
- sparkapplications
- scheduledsparkapplications
verbs:
- '*'
```

4. 创建serviceaccount

```yaml
apiVersion: v1
kind: ServiceAccount
metadata:
  name: spark-operator-sa
  namespace: spark-operator-ns
```
5. 创建clusterrolebinding

```bash
kubectl create clusterrolebinding spark-operator-rolebinding --clusterrole=spark-operator-role --serviceaccount=spark-operator-ns:spark-operator-sa
```

```yaml
apiVersion: rbac.authorization.k8s.io/v1
kind: ClusterRoleBinding
metadata:
  name: spark-operator-rolebinding
  selfLink: /apis/rbac.authorization.k8s.io/v1/clusterrolebinding/spark-operator-rolebinding
roleRef:
  apiGroup: rbac.authorization.k8s.io
  kind: ClusterRole
  name: spark-operator-role
subjects:
- kind: ServiceAccount
  name: spark-operator-sa
  namespace: spark-operator-ns
```

6. 安装crd，crd使用官方提供的就可以
- [sparkapplication](https://github.com/GoogleCloudPlatform/spark-on-k8s-operator/blob/v1beta2-1.2.2-3.0.0/manifest/crds/sparkoperator.k8s.io_sparkapplications.yaml)
- [schedulersparkapplication](https://github.com/GoogleCloudPlatform/spark-on-k8s-operator/blob/v1beta2-1.2.2-3.0.0/manifest/crds/sparkoperator.k8s.io_scheduledsparkapplications.yaml)
7. 通过yaml提交任务

```bash
#需要注意镜像位置
#jar包的位置是容器里jar包的问题，而不是本地的path
#注意operator版本一定与spark客户端一致
kubectl apply -f examples/spark-pi.yaml -n spark-operator-n
```

8. 通过命令查看运行状态

```bash
kubectl get pod -n spark-operator-ns
kubectl describe sparkapplication -n spark-operator-ns 
kubectl logs ${spark-driver-name} -n spark-operator-ns  
```