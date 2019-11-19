DOCKERHUBREPO=islamahmad
IMAGE=${DOCKERHUBREPO}/eaproj-commentms:1.0.0

# ===== Maven =====
maven-rebuild:
	mvn clean && mvn install

# ===== Docker =====
docker-build: maven-rebuild
	docker build -t ${IMAGE} .

docker-run:
	docker run -p 8080:8092 ${IMAGE}

docker-login:
	docker login

docker-push: docker-login docker-build
	docker push ${IMAGE}

k8-install:
	kubectl apply -f manifests/

k8-delete:
	kubectl delete -f manifests/

k8-repush-restart: k8-delete docker-push k8-install
