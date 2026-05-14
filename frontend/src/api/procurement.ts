import { get, post, put, del } from './request'

// ---- 采购申请 ----
export interface ProcurementRequest {
  id: number
  requestNo: string
  title: string
  description: string
  amount: number
  category: string
  status: string
  applicantId: number
  deptId: number
  currentStep: number
  totalSteps: number
  createdAt: string
  updatedAt: string
}

export interface RequestCreateDTO {
  title: string
  description?: string
  amount: number
  category?: string
}

export interface RequestUpdateDTO {
  title?: string
  description?: string
  amount?: number
  category?: string
}

export function getRequestList(params: any) {
  return get<{ records: ProcurementRequest[]; total: number }>('/procurement/requests', params)
}

export function getRequestById(id: number) {
  return get<ProcurementRequest>(`/procurement/requests/${id}`)
}

export function createRequest(data: RequestCreateDTO) {
  return post('/procurement/requests', data)
}

export function updateRequest(id: number, data: RequestUpdateDTO) {
  return put(`/procurement/requests/${id}`, data)
}

export function deleteRequest(id: number) {
  return del(`/procurement/requests/${id}`)
}

export function submitRequest(id: number) {
  return put(`/procurement/requests/${id}/submit`)
}

export function withdrawRequest(id: number) {
  return put(`/procurement/requests/${id}/withdraw`)
}

// ---- 审批记录 ----
export interface ProcurementApproval {
  id: number
  requestId: number
  stepOrder: number
  expectedRole: string
  approverId: number
  status: string
  comment: string
  approvedAt: string
  createdAt: string
}

export function getRequestApprovals(requestId: number) {
  return get<ProcurementApproval[]>(`/procurement/requests/${requestId}/approvals`)
}

export function getPendingApprovals(params: any) {
  return get<{ records: ProcurementApproval[]; total: number }>('/procurement/approvals/pending', params)
}

export function approveApproval(id: number, comment?: string) {
  return put(`/procurement/approvals/${id}/approve`, { comment })
}

export function rejectApproval(id: number, comment?: string) {
  return put(`/procurement/approvals/${id}/reject`, { comment })
}

// ---- 供应商 ----
export interface Supplier {
  id: number
  supplierNo: string
  name: string
  contactPerson: string
  contactPhone: string
  email: string
  address: string
  qualification: string
  status: number
  createdAt: string
}

export interface SupplierCreateDTO {
  name: string
  contactPerson?: string
  contactPhone?: string
  email?: string
  address?: string
  qualification?: string
}

export interface SupplierUpdateDTO {
  name?: string
  contactPerson?: string
  contactPhone?: string
  email?: string
  address?: string
  qualification?: string
}

export function getSupplierList(params: any) {
  return get<{ records: Supplier[]; total: number }>('/procurement/suppliers', params)
}

export function getSupplierById(id: number) {
  return get<Supplier>(`/procurement/suppliers/${id}`)
}

export function createSupplier(data: SupplierCreateDTO) {
  return post('/procurement/suppliers', data)
}

export function updateSupplier(id: number, data: SupplierUpdateDTO) {
  return put(`/procurement/suppliers/${id}`, data)
}

export function deleteSupplier(id: number) {
  return del(`/procurement/suppliers/${id}`)
}

export function updateSupplierStatus(id: number, status: number) {
  return put(`/procurement/suppliers/${id}/status`, null, { params: { status } })
}

// ---- 合同 ----
export interface Contract {
  id: number
  contractNo: string
  requestId: number
  supplierId: number
  title: string
  amount: number
  signedDate: string
  expiryDate: string
  status: string
  createdAt: string
  updatedAt: string
}

export interface ContractCreateDTO {
  requestId: number
  supplierId: number
  title: string
  amount: number
  signedDate?: string
  expiryDate?: string
}

export interface ContractUpdateDTO {
  title?: string
  amount?: number
  signedDate?: string
  expiryDate?: string
}

export function getContractList(params: any) {
  return get<{ records: Contract[]; total: number }>('/procurement/contracts', params)
}

export function getContractById(id: number) {
  return get<Contract>(`/procurement/contracts/${id}`)
}

export function createContract(data: ContractCreateDTO) {
  return post('/procurement/contracts', data)
}

export function updateContract(id: number, data: ContractUpdateDTO) {
  return put(`/procurement/contracts/${id}`, data)
}

export function updateContractStatus(id: number, status: string) {
  return put(`/procurement/contracts/${id}/status`, null, { params: { status } })
}

// ---- 付款节点 ----
export interface PaymentNode {
  id: number
  contractId: number
  nodeName: string
  amount: number
  ratio: number
  plannedDate: string
  actualDate: string
  status: string
  createdAt: string
}

export interface PaymentCreateDTO {
  contractId: number
  nodeName: string
  amount: number
  ratio?: number
  plannedDate?: string
}

export interface PaymentUpdateDTO {
  nodeName?: string
  amount?: number
  ratio?: number
  plannedDate?: string
}

export function getPaymentNodes(contractId: number) {
  return get<PaymentNode[]>(`/procurement/payments/contract/${contractId}`)
}

export function createPayment(data: PaymentCreateDTO) {
  return post('/procurement/payments', data)
}

export function updatePayment(id: number, data: PaymentUpdateDTO) {
  return put(`/procurement/payments/${id}`, data)
}

export function deletePayment(id: number) {
  return del(`/procurement/payments/${id}`)
}

export function confirmPayment(id: number) {
  return put(`/procurement/payments/${id}/pay`)
}
