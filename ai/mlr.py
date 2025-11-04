import os
import yaml
import mlflow
from mlflow.entities import RunStatus

mlflow.set_tracking_uri("sqlite:///mlflow.db")

def restore_runs(root_dir=r"C:\Users\SSAFY\Desktop\WANG\S13P31A106\ai\mlruns"):
    for exp_id in os.listdir(root_dir):
        exp_path = os.path.join(root_dir, exp_id)
        if not os.path.isdir(exp_path) or not exp_id.isdigit():
            continue
        
        # 실험 이름 복구
        meta_path = os.path.join(exp_path, "meta.yaml")
        with open(meta_path) as f:
            exp_meta = yaml.safe_load(f)
        exp_name = exp_meta["name"]

        # 실험 존재 확인
        existing = {e.name: e.experiment_id for e in mlflow.search_experiments()}
        if exp_name in existing:
            exp_uid = existing[exp_name]
        else:
            exp_uid = mlflow.create_experiment(exp_name, artifact_location=os.path.abspath(exp_path))
        
        # run-level 복구
        for run_id in os.listdir(exp_path):
            run_path = os.path.join(exp_path, run_id)
            if not os.path.isdir(run_path) or run_id.startswith("."):
                continue
            meta_run_path = os.path.join(run_path, "meta.yaml")
            if not os.path.exists(meta_run_path):
                continue

            with open(meta_run_path) as f:
                run_meta = yaml.safe_load(f)

            # run 기본 정보
            run_name = run_meta.get("run_name", run_id)
            start = run_meta.get("start_time")
            end = run_meta.get("end_time")
            user = run_meta.get("user_id", "unknown")

            # run 생성
            with mlflow.start_run(run_name=run_name, experiment_id=exp_uid) as r:
                run_uuid = r.info.run_id

                # params
                params_dir = os.path.join(run_path, "params")
                if os.path.exists(params_dir):
                    for pfile in os.listdir(params_dir):
                        with open(os.path.join(params_dir, pfile)) as pf:
                            mlflow.log_param(pfile, pf.read().strip())

                # metrics
                metrics_dir = os.path.join(run_path, "metrics")
                if os.path.exists(metrics_dir):
                    for mfile in os.listdir(metrics_dir):
                        with open(os.path.join(metrics_dir, mfile)) as mf:
                            lines = mf.readlines()
                            if lines:
                                # 파일 내에 step, value, timestamp 형식
                                for line in lines:
                                    val_parts = line.strip().split()
                                    if len(val_parts) >= 2:
                                        val = float(val_parts[1])
                                        mlflow.log_metric(mfile, val)

                # tags
                tags_dir = os.path.join(run_path, "tags")
                if os.path.exists(tags_dir):
                    for tfile in os.listdir(tags_dir):
                        with open(os.path.join(tags_dir, tfile)) as tf:
                            mlflow.set_tag(tfile, tf.read().strip())

            print(f"✅ Restored run {run_name} ({run_id}) under {exp_name}")

restore_runs()
print("\n🎉 All runs restored! Now run: mlflow ui --backend-store-uri sqlite:///mlflow.db --port 5000")
