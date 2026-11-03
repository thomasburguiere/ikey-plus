# Deploying ikey-plus-rest for free

## Recommended: Google Cloud Run

Cloud Run builds and runs the container as-is, scales to zero when idle, and its free tier
(about 2M requests and 360k GiB-seconds per month) easily covers a personal or demo API.
The app listens on port 8080 (`Application.java`), which is the port Cloud Run expects by
default, so nothing needs to change.

You need a GCP account with billing enabled. You won't be charged while you stay inside the
free tier.

### One-time setup

```bash
gcloud auth login
gcloud config set project YOUR_PROJECT_ID
gcloud services enable run.googleapis.com artifactregistry.googleapis.com
gcloud artifacts repositories create ikey --repository-format=docker --location=europe-west1
gcloud auth configure-docker europe-west1-docker.pkg.dev
```

### Build and push

Build from the repository root, because the Dockerfile copies `pom.xml` and both modules:

```bash
IMG=europe-west1-docker.pkg.dev/YOUR_PROJECT_ID/ikey/ikey-plus-rest
docker build --platform linux/amd64 -f ikey-plus-rest/Dockerfile -t $IMG .
docker push $IMG
```

### Deploy

```bash
gcloud run deploy ikey-plus-rest --image $IMG --region europe-west1 \
  --allow-unauthenticated --memory 512Mi --max-instances 1
```

### Things to know

- **`--platform linux/amd64` matters on a Mac.** Without it, an M-series Mac builds an ARM
  image, and Cloud Run won't start it.
- **`gcloud run deploy --source .` isn't used** because it expects a Dockerfile at the
  repository root, and this one is in `ikey-plus-rest/`.
- **`--max-instances 1` caps your spending** if the URL gets hammered. Adding a budget alert
  in the GCP billing console is also a good idea.
- **The first request after an idle period is slow** (a few seconds) while the JVM starts.
  `--cpu-boost` helps if that's a problem.

## Other options

- **Render.com (free web service):** no credit card needed. Connect the GitHub repository and
  set the Dockerfile path to `ikey-plus-rest/Dockerfile` and the build context to `.`. It
  sleeps after 15 minutes idle and takes about 30–60 seconds to wake up.
- **Koyeb (free tier):** works much like Render.
- **AWS:** there's no good free option. App Runner and Fargate have no free tier, and the
  free EC2 t2/t3.micro (1 GB of RAM) is short on memory for a Maven build plus a JVM.
