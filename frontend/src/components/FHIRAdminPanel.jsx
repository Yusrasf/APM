import { useEffect, useState } from "react";

const API_BASE = "http://localhost:8080/fhir";

export default function FHIRAdminPanel({ patientId }) {

    //
    const [serverResponse, setServerResponse] = useState(null);

    //initialize the
    async function putFHIR(resourceType, id, body) {
        const res = await fetch(`${API_BASE}/${resourceType}/${id}`, {
            method: "PUT",
            headers: { "Content-Type": "application/fhir+json" },
            body: JSON.stringify(body),
        });

        const text = await res.text();
        setServerResponse({
            ok: res.ok,
            body: text,
            status: res.status
        });
    }

    // Random ID generators
    const randomId = () => Math.floor(Math.random() * 9999);

    const genOrgId = (name) =>
        "ORG-" + name.toUpperCase().replace(/\s+/g, "-") + "-" + randomId();

    const genLocId = (name) =>
        "LOC-" + name.toUpperCase().replace(/\s+/g, "-") + "-" + randomId();

    const genEncId = (pid, date) =>
        "ENC-" + pid + "-" + date.replace(/\D/g, "");

    const genImmId = (pid, display) =>
        "IMM-" + pid + "-" + display.toUpperCase().replace(/\s+/g, "-") + "-" + randomId();

    // Organization entries
    const [org, setOrg] = useState({
        id: "",
        name: "",
        address: "",
        city: "",
        postal: "",
        country: "AT",
    });
    //  Location resource entries
    const [loc, setLoc] = useState({
        id: "",
        name: "",
        orgId: "",
        address: "",
        city: "",
        postal: "",
        country: "AT",
    });
    //Encounter resource entries
    const [enc, setEnc] = useState({
        id: "",
        date: "",
        orgId: "",
        locId: "",
    });
    //Immunization resource entries
    const [imm, setImm] = useState({
        id: "",
        code: "",
        display: "",
        date: "",
        encId: "",
    });

    // Fetch existing Organizations & Locations for Dropdowns
    const [orgList, setOrgList] = useState([]);
    const [locList, setLocList] = useState([]);

    useEffect(() => {
        async function fetchResources() {
            let o = await fetch(`${API_BASE}/Organization`).then(r => r.json());
            let l = await fetch(`${API_BASE}/Location`).then(r => r.json());

            setOrgList(o.entry?.map(e => e.resource) || []);
            setLocList(l.entry?.map(e => e.resource) || []);
        }
        fetchResources();
    }, []);

    // Validation of the entries
    const invalid = (val) => val.trim() === "";

    return (
        <div style={{ marginTop: "2rem" }}>

            <h2>Add Immunization data (Organization | Location | Encounter | Immunization)</h2>
            {/*<p>Create Organization / Location / Encounter / Immunization using PUT.</p> */}

            <div style={{
                display: "grid",
                gridTemplateColumns: "repeat(2, 1fr)",
                gap: "2rem",
                marginTop: "2rem"
            }}>

                {/*ORGANIZATION FORM */}
                <div style={{ padding: 15, border: "1px solid #ccc", borderRadius: 8 , background: "#fafafa"}}>
                    <h3>Organization</h3>

                    <input
                        placeholder="Name"
                        style={{ borderColor: invalid(org.name) ? "red" : "#ccc" }}
                        value={org.name}
                        onChange={(e) => {
                            let name = e.target.value;
                            setOrg({
                                ...org,
                                name,
                                id: genOrgId(name)
                            });
                        }}
                    />

                    <input
                        placeholder="Address line"
                        value={org.address}
                        onChange={e => setOrg({ ...org, address: e.target.value })}
                    />

                    <input
                        placeholder="City"
                        value={org.city}
                        onChange={e => setOrg({ ...org, city: e.target.value })}
                    />

                    <input
                        placeholder="Postal"
                        value={org.postal}
                        onChange={e => setOrg({ ...org, postal: e.target.value })}
                    />

                    <button
                        onClick={() =>
                            putFHIR("Organization", org.id, {
                                resourceType: "Organization",
                                id: org.id,
                                name: org.name,
                                address: [
                                    {
                                        line: [org.address],
                                        city: org.city,
                                        postalCode: org.postal,
                                        country: org.country
                                    }
                                ]
                            })
                        }
                    >
                        Save Organization
                    </button>
                </div>

                {/* LOCATION FORM */}
                <div style={{ padding: 15, border: "1px solid #ccc", borderRadius: 8 , background: "#fafafa"}}>
                    <h3>Location</h3>

                    <input
                        placeholder="Name"
                        style={{ borderColor: invalid(loc.name) ? "red" : "#ccc" }}
                        value={loc.name}
                        onChange={(e) => {
                            let name = e.target.value;
                            setLoc({
                                ...loc,
                                name,
                                id: genLocId(name)
                            });
                        }}
                    />

                    {/* Dropdown of organizations */}
                    <select
                        value={loc.orgId}
                        onChange={(e) => setLoc({ ...loc, orgId: e.target.value })}
                    >
                        <option value="">Select Managing Organization</option>
                        {orgList.map(o => (
                            <option key={o.id} value={o.id}>{o.name}</option>
                        ))}
                    </select>

                    <input
                        placeholder="Address"
                        value={loc.address}
                        onChange={e => setLoc({ ...loc, address: e.target.value })}
                    />

                    <input
                        placeholder="City"
                        value={loc.city}
                        onChange={e => setLoc({ ...loc, city: e.target.value })}
                    />

                    <input
                        placeholder="Postal"
                        value={loc.postal}
                        onChange={e => setLoc({ ...loc, postal: e.target.value })}
                    />

                    <button
                        onClick={() =>
                            putFHIR("Location", loc.id, {
                                resourceType: "Location",
                                id: loc.id,
                                name: loc.name,
                                status: "active",
                                mode: "instance",
                                address: {
                                    line: [loc.address],
                                    city: loc.city,
                                    postalCode: loc.postal,
                                    country: loc.country
                                },
                                managingOrganization: {
                                    reference: "Organization/" + loc.orgId
                                }
                            })
                        }
                    >
                        Save Location
                    </button>
                </div>

                {/* ENCOUNTER FORM */}
                <div style={{ padding: 15, border: "1px solid #ccc", borderRadius: 8 , background: "#fafafa"}}>
                    <h3>Encounter</h3>

                    <input
                        type="datetime-local"
                        value={enc.date}
                        onChange={(e) =>
                            setEnc({
                                ...enc,
                                date: e.target.value,
                                id: genEncId(patientId, e.target.value)
                            })
                        }
                    />

                    <select
                        value={enc.orgId}
                        onChange={(e) => setEnc({ ...enc, orgId: e.target.value })}
                    >
                        <option value="">Select Organization</option>
                        {orgList.map(o => (
                            <option key={o.id} value={o.id}>{o.name}</option>
                        ))}
                    </select>

                    <select
                        value={enc.locId}
                        onChange={(e) => setEnc({ ...enc, locId: e.target.value })}
                    >
                        <option value="">Select Location</option>
                        {locList.map(l => (
                            <option key={l.id} value={l.id}>{l.name}</option>
                        ))}
                    </select>

                    <button
                        onClick={() =>
                            putFHIR("Encounter", enc.id, {
                                resourceType: "Encounter",
                                id: enc.id,
                                status: "completed",
                                subject: { reference: `Patient/${patientId}` },
                                serviceProvider: { reference: `Organization/${enc.orgId}` },
                                location: [
                                    {
                                        location: {
                                            reference: `Location/${enc.locId}`
                                        }
                                    }
                                ],
                                actualPeriod: {
                                    start: enc.date ? enc.date + ":00" : undefined
                                }
                            })
                        }
                    >
                        Save Encounter
                    </button>
                </div>

                {/* IMMUNIZATION FORM */}
                <div style={{ padding: 15, border: "1px solid #ccc", borderRadius: 8 , background: "#fafafa" }}>
                    <h3>Immunization</h3>

                    <input
                        placeholder="Vaccine Display"
                        value={imm.display}
                        onChange={(e) =>
                            setImm({
                                ...imm,
                                display: e.target.value,
                                id: genImmId(patientId, e.target.value)
                            })
                        }
                    />

                    <input
                        placeholder="Vaccine Code (CVX)"
                        value={imm.code}
                        onChange={(e) => setImm({ ...imm, code: e.target.value })}
                    />

                    <input
                        type="datetime-local"
                        value={imm.date}
                        onChange={(e) => setImm({ ...imm, date: e.target.value })}
                    />

                    <input
                        placeholder="Encounter ID"
                        value={imm.encId}
                        onChange={(e) => setImm({ ...imm, encId: e.target.value })}
                    />

                    <button
                        onClick={() =>
                            putFHIR("Immunization", imm.id, {
                                resourceType: "Immunization",
                                id: imm.id,
                                status: "completed",
                                patient: { reference: `Patient/${patientId}` },
                                encounter: { reference: `Encounter/${imm.encId}` },
                                vaccineCode: {
                                    coding: [
                                        {
                                            system: "http://hl7.org/fhir/sid/cvx",
                                            code: imm.code,
                                            display: imm.display
                                        }
                                    ]
                                },
                                occurrenceDateTime: imm.date ? imm.date + ":00" : undefined
                            })
                        }
                    >
                        Save Immunization
                    </button>
                </div>
            </div>

            {/* Server Response Viewer */}
            {serverResponse && (
                <div style={{ marginTop: "2rem", padding: 10, border: "1px solid black" }}>
                    <h3>Server Response</h3>
                    <p>Status: {serverResponse.status}</p>
                    <pre style={{ whiteSpace: "pre-wrap" }}>{serverResponse.body}</pre>
                </div>
            )}

        </div>
    );
}
