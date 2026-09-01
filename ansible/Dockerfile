# Ansible's control node needs Linux; this machine is Windows with no WSL distro suitable for
# installing packages into. Rather than fight that, Ansible runs inside this small container -
# the same pattern already used elsewhere in this project (the JDK25/Windows Selector bug
# workaround) for tools that need a Linux environment this host can't provide natively.
FROM python:3.12-alpine

RUN apk add --no-cache docker-cli docker-cli-compose docker-cli-buildx bash curl \
    && pip install --no-cache-dir ansible-core==2.17.* pywinrm==0.4.*

# kind's CLI is needed client-side for `kind load docker-image` - the docker socket alone isn't
# enough, kind itself has to run to talk to the node containers over the docker API.
RUN curl -Lo /usr/local/bin/kind https://kind.sigs.k8s.io/dl/v0.24.0/kind-linux-amd64 \
    && chmod +x /usr/local/bin/kind

# Lets this same control node manage the Windows host itself over WinRM (win_* modules) -
# no separate VM needed for the Windows-administration/remote-administration resume gap.
RUN ansible-galaxy collection install ansible.windows

WORKDIR /workspace
ENTRYPOINT ["ansible-playbook"]
